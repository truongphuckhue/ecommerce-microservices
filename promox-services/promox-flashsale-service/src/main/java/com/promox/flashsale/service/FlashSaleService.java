package com.promox.flashsale.service;

import com.promox.flashsale.dto.FlashSaleRequest;
import com.promox.flashsale.dto.FlashSaleResponse;
import com.promox.flashsale.dto.PurchaseRequest;
import com.promox.flashsale.dto.PurchaseResponse;
import com.promox.flashsale.entity.FlashSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlashSaleService {

    // CRUD operations
    FlashSaleResponse createFlashSale(FlashSaleRequest request);
    
    FlashSaleResponse getFlashSaleById(Long id);
    
    Page<FlashSaleResponse> getAllFlashSales(Pageable pageable);
    
    FlashSaleResponse updateFlashSale(Long id, FlashSaleRequest request);
    
    void deleteFlashSale(Long id);

    // Purchase operations (with Redis)
    PurchaseResponse purchaseFlashSale(Long flashSaleId, PurchaseRequest request);
    
    void releaseReservedStock(Long flashSaleId, Long userId, Integer quantity);

    // Status management
    FlashSaleResponse activateFlashSale(Long id);
    
    FlashSaleResponse endFlashSale(Long id);

    // Query operations
    List<FlashSaleResponse> getActiveFlashSales();
    
    List<FlashSaleResponse> getFlashSalesByCampaign(Long campaignId);
    
    List<FlashSaleResponse> getFlashSalesByStatus(FlashSale.FlashSaleStatus status);
    
    List<FlashSaleResponse> getUpcomingFlashSales(int limit);
    
    Page<FlashSaleResponse> searchFlashSales(String keyword, Pageable pageable);

    // Stock management
    Integer getAvailableStock(Long flashSaleId);
    
    void syncStockToDatabase(Long flashSaleId);

    // Scheduler tasks
    void startScheduledFlashSales();
    
    void endExpiredFlashSales();
    
    void syncAllStockToDatabase();
}
