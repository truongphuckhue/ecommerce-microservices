package com.promox.flashsale.controller;

import com.promox.flashsale.dto.*;
import com.promox.flashsale.entity.FlashSale;
import com.promox.flashsale.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flash-sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flash Sale Management", description = "APIs for managing flash sales with Redis atomic operations")
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PostMapping
    @Operation(summary = "Create flash sale", description = "Create a new flash sale event")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> createFlashSale(
            @Valid @RequestBody FlashSaleRequest request) {
        log.info("REST request to create flash sale: {}", request.getProductName());

        FlashSaleResponse response = flashSaleService.createFlashSale(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flash sale created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flash sale by ID", description = "Retrieve flash sale details with real-time stock")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> getFlashSaleById(@PathVariable Long id) {
        log.info("REST request to get flash sale: {}", id);

        FlashSaleResponse response = flashSaleService.getFlashSaleById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all flash sales", description = "Retrieve all flash sales with pagination")
    public ResponseEntity<ApiResponse<Page<FlashSaleResponse>>> getAllFlashSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("REST request to get all flash sales, page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FlashSaleResponse> flashSales = flashSaleService.getAllFlashSales(pageable);

        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update flash sale", description = "Update existing flash sale")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> updateFlashSale(
            @PathVariable Long id,
            @Valid @RequestBody FlashSaleRequest request) {

        log.info("REST request to update flash sale: {}", id);

        FlashSaleResponse response = flashSaleService.updateFlashSale(id, request);

        return ResponseEntity.ok(ApiResponse.success("Flash sale updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete flash sale", description = "Cancel a flash sale")
    public ResponseEntity<ApiResponse<Void>> deleteFlashSale(@PathVariable Long id) {
        log.info("REST request to delete flash sale: {}", id);

        flashSaleService.deleteFlashSale(id);

        return ResponseEntity.ok(ApiResponse.success("Flash sale cancelled successfully", null));
    }

    // Purchase operations

    @PostMapping("/{id}/purchase")
    @Operation(summary = "Purchase flash sale", description = "Make a purchase with atomic Redis operations and distributed locking")
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseFlashSale(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseRequest request) {

        log.info("REST request to purchase flash sale: {}", id);

        PurchaseResponse response = flashSaleService.purchaseFlashSale(id, request);

        if (response.getSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PurchaseResponse>builder()
                            .success(false)
                            .message(response.getMessage())
                            .data(response)
                            .build());
        }
    }

    // Status management

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate flash sale", description = "Manually activate a scheduled flash sale")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> activateFlashSale(@PathVariable Long id) {
        log.info("REST request to activate flash sale: {}", id);

        FlashSaleResponse response = flashSaleService.activateFlashSale(id);

        return ResponseEntity.ok(ApiResponse.success("Flash sale activated successfully", response));
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "End flash sale", description = "Manually end an active flash sale")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> endFlashSale(@PathVariable Long id) {
        log.info("REST request to end flash sale: {}", id);

        FlashSaleResponse response = flashSaleService.endFlashSale(id);

        return ResponseEntity.ok(ApiResponse.success("Flash sale ended successfully", response));
    }

    // Query operations

    @GetMapping("/active")
    @Operation(summary = "Get active flash sales", description = "Retrieve all currently active flash sales")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getActiveFlashSales() {
        log.info("REST request to get active flash sales");

        List<FlashSaleResponse> flashSales = flashSaleService.getActiveFlashSales();

        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming flash sales", description = "Retrieve upcoming scheduled flash sales")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getUpcomingFlashSales(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("REST request to get upcoming flash sales, limit: {}", limit);

        List<FlashSaleResponse> flashSales = flashSaleService.getUpcomingFlashSales(limit);

        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @GetMapping("/campaign/{campaignId}")
    @Operation(summary = "Get flash sales by campaign", description = "Retrieve flash sales for a specific campaign")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getFlashSalesByCampaign(
            @PathVariable Long campaignId) {

        log.info("REST request to get flash sales for campaign: {}", campaignId);

        List<FlashSaleResponse> flashSales = flashSaleService.getFlashSalesByCampaign(campaignId);

        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get flash sales by status", description = "Retrieve flash sales by status")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getFlashSalesByStatus(
            @PathVariable FlashSale.FlashSaleStatus status) {

        log.info("REST request to get flash sales by status: {}", status);

        List<FlashSaleResponse> flashSales = flashSaleService.getFlashSalesByStatus(status);

        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    @GetMapping("/search")
    @Operation(summary = "Search flash sales", description = "Search flash sales by product name")
    public ResponseEntity<ApiResponse<Page<FlashSaleResponse>>> searchFlashSales(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("REST request to search flash sales with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size);
        Page<FlashSaleResponse> flashSales = flashSaleService.searchFlashSales(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }

    // Stock management

    @GetMapping("/{id}/stock")
    @Operation(summary = "Get available stock", description = "Get real-time available stock from Redis")
    public ResponseEntity<ApiResponse<Integer>> getAvailableStock(@PathVariable Long id) {
        log.info("REST request to get available stock for flash sale: {}", id);

        Integer stock = flashSaleService.getAvailableStock(id);

        return ResponseEntity.ok(ApiResponse.success(stock));
    }

    @PostMapping("/{id}/sync-stock")
    @Operation(summary = "Sync stock to database", description = "Sync Redis stock to database")
    public ResponseEntity<ApiResponse<Void>> syncStockToDatabase(@PathVariable Long id) {
        log.info("REST request to sync stock to database for flash sale: {}", id);

        flashSaleService.syncStockToDatabase(id);

        return ResponseEntity.ok(ApiResponse.success("Stock synced successfully", null));
    }
}
