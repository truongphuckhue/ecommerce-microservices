package com.ecommerce.inventory.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryTransaction;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * STRATEGY 1: OPTIMISTIC LOCKING with @Version
 * 
 * Pros:
 * - Better performance (no database locks)
 * - Higher concurrency
 * - Suitable for low-conflict scenarios
 * 
 * Cons:
 * - Requires retry logic
 * - May fail under high contention
 * - Not suitable for high-conflict operations
 * 
 * Use when: Read-heavy workload, low conflict probability
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class OptimisticLockingInventoryService implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        log.info("Creating inventory for product ID: {}", request.getProductId());

        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new BusinessException("INVENTORY_EXISTS", 
                "Inventory for product " + request.getProductId() + " already exists");
        }

        if (inventoryRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU_EXISTS", 
                "SKU " + request.getSku() + " already exists");
        }

        Inventory inventory = Inventory.builder()
                .productId(request.getProductId())
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .reservedQuantity(0)
                .availableQuantity(request.getQuantity())
                .reorderPoint(request.getReorderPoint())
                .reorderQuantity(request.getReorderQuantity())
                .location(request.getLocation())
                .build();

        inventory = inventoryRepository.save(inventory);

        // Create initial transaction
        createTransaction(inventory, InventoryTransaction.TransactionType.STOCK_IN,
                request.getQuantity(), 0, request.getQuantity(), null, null, "Initial stock");

        log.info("Inventory created: ID={}, ProductID={}", inventory.getId(), inventory.getProductId());
        
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        log.info("Updating inventory ID: {}", id);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        // Check SKU uniqueness if changed
        if (!inventory.getSku().equals(request.getSku()) && 
            inventoryRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU_EXISTS", "SKU " + request.getSku() + " already exists");
        }

        inventory.setSku(request.getSku());
        inventory.setReorderPoint(request.getReorderPoint());
        inventory.setReorderQuantity(request.getReorderQuantity());
        inventory.setLocation(request.getLocation());

        inventory = inventoryRepository.save(inventory);
        
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        log.info("Deleting inventory ID: {}", id);

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));

        if (inventory.getReservedQuantity() > 0) {
            throw new BusinessException("CANNOT_DELETE", 
                "Cannot delete inventory with reserved stock");
        }

        inventoryRepository.delete(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySku(String sku) {
        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "sku", sku));
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(InventoryResponse::fromInventory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Retryable(
        value = {OptimisticLockingFailureException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public InventoryResponse addStock(Long productId, StockUpdateRequest request) {
        log.info("Adding stock to product {}: quantity={}", productId, request.getQuantity());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        int quantityBefore = inventory.getQuantity();
        
        inventory.addStock(request.getQuantity());
        
        inventory = inventoryRepository.save(inventory);

        createTransaction(inventory, InventoryTransaction.TransactionType.STOCK_IN,
                request.getQuantity(), quantityBefore, inventory.getQuantity(),
                null, null, request.getNotes());

        log.info("Stock added successfully: ProductID={}, NewQuantity={}", productId, inventory.getQuantity());
        
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional
    @Retryable(
        value = {OptimisticLockingFailureException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public InventoryResponse removeStock(Long productId, StockUpdateRequest request) {
        log.info("Removing stock from product {}: quantity={}", productId, request.getQuantity());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        int quantityBefore = inventory.getQuantity();
        
        inventory.removeStock(request.getQuantity());
        
        inventory = inventoryRepository.save(inventory);

        createTransaction(inventory, InventoryTransaction.TransactionType.STOCK_OUT,
                request.getQuantity(), quantityBefore, inventory.getQuantity(),
                null, null, request.getNotes());

        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional
    @Retryable(
        value = {OptimisticLockingFailureException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public InventoryResponse reserveStock(Long productId, ReservationRequest request) {
        log.info("Reserving stock for product {}: quantity={}, referenceId={}", 
                productId, request.getQuantity(), request.getReferenceId());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        if (!inventory.canReserve(request.getQuantity())) {
            throw new BusinessException("INSUFFICIENT_STOCK", 
                "Insufficient available stock. Available: " + inventory.getAvailableQuantity() +
                ", Requested: " + request.getQuantity());
        }

        int quantityBefore = inventory.getQuantity();
        
        inventory.reserve(request.getQuantity());
        
        inventory = inventoryRepository.save(inventory);

        createTransaction(inventory, InventoryTransaction.TransactionType.RESERVATION,
                request.getQuantity(), quantityBefore, inventory.getQuantity(),
                request.getReferenceId(), request.getReferenceType(), 
                "Stock reserved");

        log.info("Stock reserved successfully: ProductID={}, ReservedQty={}", 
                productId, inventory.getReservedQuantity());
        
        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional
    @Retryable(
        value = {OptimisticLockingFailureException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public InventoryResponse releaseReservation(Long productId, ReservationRequest request) {
        log.info("Releasing reservation for product {}: quantity={}, referenceId={}", 
                productId, request.getQuantity(), request.getReferenceId());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        int quantityBefore = inventory.getQuantity();
        
        inventory.releaseReservation(request.getQuantity());
        
        inventory = inventoryRepository.save(inventory);

        createTransaction(inventory, InventoryTransaction.TransactionType.RELEASE_RESERVATION,
                request.getQuantity(), quantityBefore, inventory.getQuantity(),
                request.getReferenceId(), request.getReferenceType(), 
                "Reservation released");

        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional
    @Retryable(
        value = {OptimisticLockingFailureException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public InventoryResponse confirmReservation(Long productId, ReservationRequest request) {
        log.info("Confirming reservation for product {}: quantity={}, referenceId={}", 
                productId, request.getQuantity(), request.getReferenceId());

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        int quantityBefore = inventory.getQuantity();
        
        inventory.confirmReservation(request.getQuantity());
        
        inventory = inventoryRepository.save(inventory);

        createTransaction(inventory, InventoryTransaction.TransactionType.CONFIRM_RESERVATION,
                request.getQuantity(), quantityBefore, inventory.getQuantity(),
                request.getReferenceId(), request.getReferenceType(), 
                "Reservation confirmed and stock deducted");

        return InventoryResponse.fromInventory(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(InventoryResponse::fromInventory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems().stream()
                .map(InventoryResponse::fromInventory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkAvailability(Long productId, Integer quantity) {
        return inventoryRepository.hasAvailableStock(productId, quantity);
    }

    private void createTransaction(Inventory inventory, InventoryTransaction.TransactionType type,
                                   Integer quantity, Integer quantityBefore, Integer quantityAfter,
                                   String referenceId, String referenceType, String notes) {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .inventory(inventory)
                .type(type)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .notes(notes)
                .build();

        transactionRepository.save(transaction);
    }
}
