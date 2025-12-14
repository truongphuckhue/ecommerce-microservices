package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.*;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
//@RequiredArgsConstructor
@Validated
@Tag(name = "Inventory Management", description = "APIs for inventory operations")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(@Qualifier("smartInventoryService") InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory by product ID")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }
    
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get inventory by SKU")
    public ResponseEntity<InventoryResponse> getInventoryBySku(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getInventoryBySku(sku));
    }
    
    @GetMapping
    @Operation(summary = "Get all inventory")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
    
    @PostMapping
    @Operation(summary = "Create new inventory")
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.createInventory(request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update inventory")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
    
    // ==================== STOCK OPERATIONS ====================
    
    @PostMapping("/{productId}/add-stock")
    @Operation(summary = "Add stock to inventory")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.addStock(productId, request));
    }
    
    @PostMapping("/{productId}/remove-stock")
    @Operation(summary = "Remove stock from inventory")
    public ResponseEntity<InventoryResponse> removeStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.removeStock(productId, request));
    }
    
    // ==================== RESERVATION OPERATIONS ====================
    
    @PostMapping("/{productId}/reserve")
    @Operation(summary = "Reserve stock for order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock reserved successfully"),
        @ApiResponse(responseCode = "400", description = "Insufficient stock"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(productId, request));
    }
    
    @PostMapping("/{productId}/release")
    @Operation(summary = "Release reserved stock")
    public ResponseEntity<InventoryResponse> releaseReservation(
            @PathVariable Long productId,
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(inventoryService.releaseReservation(productId, request));
    }
    
    @PostMapping("/{productId}/confirm")
    @Operation(summary = "Confirm reservation and deduct stock")
    public ResponseEntity<InventoryResponse> confirmReservation(
            @PathVariable Long productId,
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(inventoryService.confirmReservation(productId, request));
    }
    
    // ==================== QUERIES ====================
    
    @GetMapping("/check/{productId}")
    @Operation(summary = "Check stock availability")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.checkAvailability(productId, quantity));
    }
    
    @GetMapping("/low-stock")
    @Operation(summary = "Get items with low stock")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }
    
    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock items")
    public ResponseEntity<List<InventoryResponse>> getOutOfStockItems() {
        return ResponseEntity.ok(inventoryService.getOutOfStockItems());
    }
}
