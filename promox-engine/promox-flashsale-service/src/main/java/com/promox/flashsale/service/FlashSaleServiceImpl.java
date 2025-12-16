package com.promox.flashsale.service;

import com.promox.flashsale.dto.*;
import com.promox.flashsale.entity.FlashSale;
import com.promox.flashsale.entity.FlashSalePurchase;
import com.promox.flashsale.exception.FlashSaleNotFoundException;
import com.promox.flashsale.mapper.FlashSaleMapper;
import com.promox.flashsale.repository.FlashSalePurchaseRepository;
import com.promox.flashsale.repository.FlashSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSalePurchaseRepository purchaseRepository;
    private final FlashSaleMapper flashSaleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "flash_sale:stock:";
    private static final String LOCK_KEY_PREFIX = "flash_sale:lock:";
    private static final long LOCK_TIMEOUT = 10; // 10 seconds

    @Override
    @Transactional
    public FlashSaleResponse createFlashSale(FlashSaleRequest request) {
        log.info("Creating new flash sale: {}", request.getProductName());

        // Validate dates
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        if (request.getFlashPrice().compareTo(request.getOriginalPrice()) >= 0) {
            throw new IllegalArgumentException("Flash price must be less than original price");
        }

        FlashSale flashSale = flashSaleMapper.toEntity(request);
        flashSale.calculateDiscountDetails();

        // Auto-activate if start time is now or in the past
        if (flashSale.getStartTime().isBefore(LocalDateTime.now()) ||
                flashSale.getStartTime().isEqual(LocalDateTime.now())) {
            flashSale.setStatus(FlashSale.FlashSaleStatus.ACTIVE);
        }

        FlashSale savedFlashSale = flashSaleRepository.save(flashSale);

        // Initialize stock in Redis
        initializeStockInRedis(savedFlashSale.getId(), savedFlashSale.getTotalQuantity());

        log.info("Flash sale created successfully with id: {}", savedFlashSale.getId());

        return flashSaleMapper.toResponse(savedFlashSale);
    }

    @Override
    @Transactional(readOnly = true)
    public FlashSaleResponse getFlashSaleById(Long id) {
        log.info("Fetching flash sale with id: {}", id);

        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new FlashSaleNotFoundException(id));

        // Get real-time stock from Redis
        Integer redisStock = getStockFromRedis(id);
        if (redisStock != null) {
            flashSale.setSoldQuantity(flashSale.getTotalQuantity() - redisStock);
        }

        return flashSaleMapper.toResponse(flashSale);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlashSaleResponse> getAllFlashSales(Pageable pageable) {
        log.info("Fetching all flash sales, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        return flashSaleRepository.findAll(pageable)
                .map(flashSaleMapper::toResponse);
    }

    @Override
    @Transactional
    public FlashSaleResponse updateFlashSale(Long id, FlashSaleRequest request) {
        log.info("Updating flash sale with id: {}", id);

        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new FlashSaleNotFoundException(id));

        // Cannot update active or ended flash sales
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot update active flash sale");
        }
        if (flashSale.getStatus() == FlashSale.FlashSaleStatus.ENDED) {
            throw new IllegalArgumentException("Cannot update ended flash sale");
        }

        flashSaleMapper.updateEntityFromRequest(flashSale, request);
        flashSale.calculateDiscountDetails();

        FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);

        // Update Redis stock if quantity changed
        if (request.getTotalQuantity() != null) {
            updateStockInRedis(id, updatedFlashSale.getTotalQuantity() - updatedFlashSale.getSoldQuantity());
        }

        log.info("Flash sale updated successfully: {}", updatedFlashSale.getId());

        return flashSaleMapper.toResponse(updatedFlashSale);
    }

    @Override
    @Transactional
    public void deleteFlashSale(Long id) {
        log.info("Deleting flash sale with id: {}", id);

        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new FlashSaleNotFoundException(id));

        // Soft delete: change status to CANCELLED
        flashSale.setStatus(FlashSale.FlashSaleStatus.CANCELLED);
        flashSaleRepository.save(flashSale);

        // Remove from Redis
        deleteStockFromRedis(id);

        log.info("Flash sale cancelled successfully: {}", id);
    }

    @Override
    @Transactional
    public PurchaseResponse purchaseFlashSale(Long flashSaleId, PurchaseRequest request) {
        log.info("Processing purchase for flash sale: {}, user: {}, quantity: {}",
                flashSaleId, request.getUserId(), request.getQuantity());

        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new FlashSaleNotFoundException(flashSaleId));

        // Validate flash sale status
        if (!flashSale.isActive()) {
            return PurchaseResponse.builder()
                    .success(false)
                    .message("Flash sale is not active")
                    .build();
        }

        // Check per user limit
        if (flashSale.getPerUserLimit() != null) {
            Integer userPurchased = purchaseRepository.countUserPurchaseQuantity(flashSaleId, request.getUserId());
            if (userPurchased + request.getQuantity() > flashSale.getPerUserLimit()) {
                return PurchaseResponse.builder()
                        .success(false)
                        .message("Exceeded per user limit. You can only purchase " + 
                                flashSale.getPerUserLimit() + " items.")
                        .build();
            }
        }

        // Acquire distributed lock
        String lockKey = LOCK_KEY_PREFIX + flashSaleId + ":" + request.getUserId();
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", LOCK_TIMEOUT, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            return PurchaseResponse.builder()
                    .success(false)
                    .message("Purchase already in progress. Please wait.")
                    .build();
        }

        try {
            // Atomic stock decrement in Redis
            String stockKey = STOCK_KEY_PREFIX + flashSaleId;
            Long remainingStock = redisTemplate.opsForValue().decrement(stockKey, request.getQuantity());

            if (remainingStock == null || remainingStock < 0) {
                // Rollback
                redisTemplate.opsForValue().increment(stockKey, request.getQuantity());
                
                return PurchaseResponse.builder()
                        .success(false)
                        .message("Sold out! Stock not available.")
                        .build();
            }

            // Calculate amounts
            BigDecimal totalAmount = flashSale.getFlashPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity()));
            BigDecimal savings = flashSale.getSavingsAmount()
                    .multiply(BigDecimal.valueOf(request.getQuantity()));

            // Create purchase record
            FlashSalePurchase purchase = FlashSalePurchase.builder()
                    .flashSaleId(flashSaleId)
                    .userId(request.getUserId())
                    .quantity(request.getQuantity())
                    .unitPrice(flashSale.getFlashPrice())
                    .totalAmount(totalAmount)
                    .savings(savings)
                    .build();

            FlashSalePurchase savedPurchase = purchaseRepository.save(purchase);

            // Update sold quantity in database
            flashSale.setSoldQuantity(flashSale.getSoldQuantity() + request.getQuantity());
            flashSaleRepository.save(flashSale);

            // Check if sold out
            if (remainingStock == 0) {
                flashSale.setStatus(FlashSale.FlashSaleStatus.SOLD_OUT);
                flashSaleRepository.save(flashSale);
            }

            log.info("Purchase completed successfully. Purchase ID: {}", savedPurchase.getId());

            return PurchaseResponse.builder()
                    .success(true)
                    .message("Purchase completed successfully!")
                    .purchaseId(savedPurchase.getId())
                    .flashSaleId(flashSaleId)
                    .userId(request.getUserId())
                    .quantity(request.getQuantity())
                    .unitPrice(flashSale.getFlashPrice())
                    .totalAmount(totalAmount)
                    .savings(savings)
                    .remainingStock(remainingStock.intValue())
                    .purchasedAt(savedPurchase.getPurchasedAt())
                    .build();

        } finally {
            // Release lock
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public void releaseReservedStock(Long flashSaleId, Long userId, Integer quantity) {
        log.info("Releasing reserved stock for flash sale: {}, user: {}, quantity: {}",
                flashSaleId, userId, quantity);

        // Increment Redis stock
        String stockKey = STOCK_KEY_PREFIX + flashSaleId;
        redisTemplate.opsForValue().increment(stockKey, quantity);

        log.info("Reserved stock released successfully");
    }

    @Override
    @Transactional
    public FlashSaleResponse activateFlashSale(Long id) {
        log.info("Activating flash sale with id: {}", id);

        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new FlashSaleNotFoundException(id));

        if (flashSale.getStatus() != FlashSale.FlashSaleStatus.SCHEDULED) {
            throw new IllegalArgumentException("Only scheduled flash sales can be activated");
        }

        LocalDateTime now = LocalDateTime.now();
        if (flashSale.getStartTime().isAfter(now)) {
            throw new IllegalArgumentException("Cannot activate before start time");
        }
        if (flashSale.getEndTime().isBefore(now)) {
            throw new IllegalArgumentException("Cannot activate expired flash sale");
        }

        flashSale.setStatus(FlashSale.FlashSaleStatus.ACTIVE);
        FlashSale activatedFlashSale = flashSaleRepository.save(flashSale);

        // Initialize Redis stock
        initializeStockInRedis(id, flashSale.getTotalQuantity() - flashSale.getSoldQuantity());

        log.info("Flash sale activated successfully: {}", id);

        return flashSaleMapper.toResponse(activatedFlashSale);
    }

    @Override
    @Transactional
    public FlashSaleResponse endFlashSale(Long id) {
        log.info("Ending flash sale with id: {}", id);

        FlashSale flashSale = flashSaleRepository.findById(id)
                .orElseThrow(() -> new FlashSaleNotFoundException(id));

        flashSale.setStatus(FlashSale.FlashSaleStatus.ENDED);
        FlashSale endedFlashSale = flashSaleRepository.save(flashSale);

        // Sync final stock and remove from Redis
        syncStockToDatabase(id);
        deleteStockFromRedis(id);

        log.info("Flash sale ended successfully: {}", id);

        return flashSaleMapper.toResponse(endedFlashSale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getActiveFlashSales() {
        log.info("Fetching active flash sales");

        return flashSaleRepository.findActiveFlashSales(LocalDateTime.now())
                .stream()
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getFlashSalesByCampaign(Long campaignId) {
        log.info("Fetching flash sales for campaign: {}", campaignId);

        return flashSaleRepository.findByCampaignId(campaignId)
                .stream()
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getFlashSalesByStatus(FlashSale.FlashSaleStatus status) {
        log.info("Fetching flash sales by status: {}", status);

        return flashSaleRepository.findByStatus(status)
                .stream()
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleResponse> getUpcomingFlashSales(int limit) {
        log.info("Fetching upcoming flash sales, limit: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        return flashSaleRepository.findUpcomingSales(LocalDateTime.now(), pageable)
                .stream()
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlashSaleResponse> searchFlashSales(String keyword, Pageable pageable) {
        log.info("Searching flash sales with keyword: {}", keyword);

        return flashSaleRepository.searchByProductName(keyword, pageable)
                .map(flashSaleMapper::toResponse);
    }

    @Override
    public Integer getAvailableStock(Long flashSaleId) {
        Integer stock = getStockFromRedis(flashSaleId);
        if (stock == null) {
            FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                    .orElseThrow(() -> new FlashSaleNotFoundException(flashSaleId));
            return flashSale.getAvailableQuantity();
        }
        return stock;
    }

    @Override
    @Transactional
    public void syncStockToDatabase(Long flashSaleId) {
        log.info("Syncing stock to database for flash sale: {}", flashSaleId);

        Integer redisStock = getStockFromRedis(flashSaleId);
        if (redisStock == null) return;

        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new FlashSaleNotFoundException(flashSaleId));

        flashSale.setSoldQuantity(flashSale.getTotalQuantity() - redisStock);
        flashSaleRepository.save(flashSale);

        log.info("Stock synced successfully. Sold: {}", flashSale.getSoldQuantity());
    }

    @Override
    @Transactional
    public void startScheduledFlashSales() {
        log.info("Running scheduled task to start flash sales");

        List<FlashSale> flashSalesToStart = flashSaleRepository
                .findFlashSalesToStart(LocalDateTime.now());

        for (FlashSale flashSale : flashSalesToStart) {
            flashSale.setStatus(FlashSale.FlashSaleStatus.ACTIVE);
            flashSaleRepository.save(flashSale);

            // Initialize Redis stock
            initializeStockInRedis(flashSale.getId(), 
                    flashSale.getTotalQuantity() - flashSale.getSoldQuantity());

            log.info("Auto-started flash sale: {} - {}", flashSale.getId(), flashSale.getProductName());
        }

        log.info("Started {} flash sales", flashSalesToStart.size());
    }

    @Override
    @Transactional
    public void endExpiredFlashSales() {
        log.info("Running scheduled task to end expired flash sales");

        List<FlashSale> flashSalesToEnd = flashSaleRepository
                .findFlashSalesToEnd(LocalDateTime.now());

        for (FlashSale flashSale : flashSalesToEnd) {
            flashSale.setStatus(FlashSale.FlashSaleStatus.ENDED);
            flashSaleRepository.save(flashSale);

            // Sync and remove from Redis
            syncStockToDatabase(flashSale.getId());
            deleteStockFromRedis(flashSale.getId());

            log.info("Auto-ended flash sale: {} - {}", flashSale.getId(), flashSale.getProductName());
        }

        log.info("Ended {} flash sales", flashSalesToEnd.size());
    }

    @Override
    @Transactional
    public void syncAllStockToDatabase() {
        log.info("Running scheduled task to sync all stock to database");

        List<FlashSale> activeFlashSales = flashSaleRepository
                .findByStatus(FlashSale.FlashSaleStatus.ACTIVE);

        for (FlashSale flashSale : activeFlashSales) {
            syncStockToDatabase(flashSale.getId());
        }

        log.info("Synced stock for {} active flash sales", activeFlashSales.size());
    }

    // Redis helper methods
    private void initializeStockInRedis(Long flashSaleId, Integer quantity) {
        String stockKey = STOCK_KEY_PREFIX + flashSaleId;
        redisTemplate.opsForValue().set(stockKey, quantity);
        log.debug("Initialized Redis stock for flash sale {}: {}", flashSaleId, quantity);
    }

    private void updateStockInRedis(Long flashSaleId, Integer quantity) {
        String stockKey = STOCK_KEY_PREFIX + flashSaleId;
        redisTemplate.opsForValue().set(stockKey, quantity);
        log.debug("Updated Redis stock for flash sale {}: {}", flashSaleId, quantity);
    }

    private Integer getStockFromRedis(Long flashSaleId) {
        String stockKey = STOCK_KEY_PREFIX + flashSaleId;
        Object stock = redisTemplate.opsForValue().get(stockKey);
        return stock != null ? (Integer) stock : null;
    }

    private void deleteStockFromRedis(Long flashSaleId) {
        String stockKey = STOCK_KEY_PREFIX + flashSaleId;
        redisTemplate.delete(stockKey);
        log.debug("Deleted Redis stock for flash sale {}", flashSaleId);
    }
}
