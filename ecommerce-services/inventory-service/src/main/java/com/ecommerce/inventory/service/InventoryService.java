package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.*;

import java.util.List;

public interface InventoryService {
    
    InventoryResponse createInventory(InventoryRequest request);
    
    InventoryResponse updateInventory(Long id, InventoryRequest request);
    
    void deleteInventory(Long id);
    
    InventoryResponse getInventoryById(Long id);
    
    InventoryResponse getInventoryByProductId(Long productId);
    
    InventoryResponse getInventoryBySku(String sku);
    
    List<InventoryResponse> getAllInventory();
    
    // Stock management
    InventoryResponse addStock(Long productId, StockUpdateRequest request);
    
    InventoryResponse removeStock(Long productId, StockUpdateRequest request);
    
    // Reservation management (3 strategies available)
    InventoryResponse reserveStock(Long productId, ReservationRequest request);
    
    InventoryResponse releaseReservation(Long productId, ReservationRequest request);
    
    InventoryResponse confirmReservation(Long productId, ReservationRequest request);
    
    // Queries
    List<InventoryResponse> getLowStockItems();
    
    List<InventoryResponse> getOutOfStockItems();
    
    boolean checkAvailability(Long productId, Integer quantity);
}
