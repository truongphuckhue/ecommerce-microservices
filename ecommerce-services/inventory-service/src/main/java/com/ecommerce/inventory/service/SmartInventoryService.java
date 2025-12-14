package com.ecommerce.inventory.service;

import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SMART INVENTORY SERVICE
 * Strategy: Thử Optimistic Lock trước, nếu fail nhiều lần thì fallback sang Pessimistic Lock
 * 
 * Flow:
 * 1. Try OptimisticLockingInventoryService (fast, retry 3 lần)
 * 2. If still fails → Fallback to PessimisticLockingInventoryService (slower but guaranteed)
 */
@Service("smartInventoryService")
@RequiredArgsConstructor
@Slf4j
public class SmartInventoryService implements InventoryService {
    
    private final OptimisticLockingInventoryService optimisticService;
    private final PessimisticLockingInventoryService pessimisticService;
    
    private static final int MAX_OPTIMISTIC_FAILURES = 3;
    
    // ==================== STRATEGY PATTERN ====================
    
    /**
     * Try optimistic first, fallback to pessimistic if it fails
     */
    @Override
    public InventoryResponse reserveStock(Long productId, ReservationRequest request) {
        try {
            // 🎯 STEP 1: Try OPTIMISTIC first (fast path)
            log.info("📊 [SMART] Attempting OPTIMISTIC locking for product: {}", productId);
            return optimisticService.reserveStock(productId, request);
            
        } catch (OptimisticLockingFailureException e) {
            // ⚠️ STEP 2: Optimistic failed after 3 retries → Fallback to PESSIMISTIC
            log.warn("⚠️ [SMART] OPTIMISTIC locking failed after {} attempts", MAX_OPTIMISTIC_FAILURES);
            log.info("🔄 [SMART] Falling back to PESSIMISTIC locking...");
            
            try {
                // Use pessimistic lock (guaranteed to succeed but slower)
                InventoryResponse response = pessimisticService.reserveStock(productId, request);
                log.info("✅ [SMART] PESSIMISTIC locking succeeded for product: {}", productId);
                return response;
                
            } catch (Exception pessimisticException) {
                log.error("❌ [SMART] Both OPTIMISTIC and PESSIMISTIC failed for product: {}", productId);
                throw pessimisticException;
            }
        }
    }
    
    @Override
    public InventoryResponse releaseReservation(Long productId, ReservationRequest request) {
        try {
            return optimisticService.releaseReservation(productId, request);
        } catch (OptimisticLockingFailureException e) {
            log.warn("⚠️ [SMART] Fallback to PESSIMISTIC for release");
            return pessimisticService.releaseReservation(productId, request);
        }
    }
    
    @Override
    public InventoryResponse confirmReservation(Long productId, ReservationRequest request) {
        try {
            return optimisticService.confirmReservation(productId, request);
        } catch (OptimisticLockingFailureException e) {
            log.warn("⚠️ [SMART] Fallback to PESSIMISTIC for confirm");
            return pessimisticService.confirmReservation(productId, request);
        }
    }
    
    @Override
    public InventoryResponse addStock(Long productId, StockUpdateRequest request) {
        try {
            return optimisticService.addStock(productId, request);
        } catch (OptimisticLockingFailureException e) {
            log.warn("⚠️ [SMART] Fallback to PESSIMISTIC for addStock");
            return pessimisticService.addStock(productId, request);
        }
    }
    
    @Override
    public InventoryResponse removeStock(Long productId, StockUpdateRequest request) {
        try {
            return optimisticService.removeStock(productId, request);
        } catch (OptimisticLockingFailureException e) {
            log.warn("⚠️ [SMART] Fallback to PESSIMISTIC for removeStock");
            return pessimisticService.removeStock(productId, request);
        }
    }
    
    // ==================== DELEGATED METHODS (No retry needed) ====================
    
    @Override
    public InventoryResponse createInventory(InventoryRequest request) {
        return optimisticService.createInventory(request);
    }
    
    @Override
    public InventoryResponse updateInventory(Long id, InventoryRequest request) {
        return optimisticService.updateInventory(id, request);
    }
    
    @Override
    public void deleteInventory(Long id) {
        optimisticService.deleteInventory(id);
    }
    
    @Override
    public InventoryResponse getInventoryById(Long id) {
        return optimisticService.getInventoryById(id);
    }
    
    @Override
    public InventoryResponse getInventoryByProductId(Long productId) {
        return optimisticService.getInventoryByProductId(productId);
    }
    
    @Override
    public InventoryResponse getInventoryBySku(String sku) {
        return optimisticService.getInventoryBySku(sku);
    }
    
    @Override
    public List<InventoryResponse> getAllInventory() {
        return optimisticService.getAllInventory();
    }
    
    @Override
    public List<InventoryResponse> getLowStockItems() {
        return optimisticService.getLowStockItems();
    }
    
    @Override
    public List<InventoryResponse> getOutOfStockItems() {
        return optimisticService.getOutOfStockItems();
    }
    
    @Override
    public boolean checkAvailability(Long productId, Integer quantity) {
        return optimisticService.checkAvailability(productId, quantity);
    }
}
