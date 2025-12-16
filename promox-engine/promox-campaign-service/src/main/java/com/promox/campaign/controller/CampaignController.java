package com.promox.campaign.controller;

import com.promox.campaign.dto.ApiResponse;
import com.promox.campaign.dto.CampaignRequest;
import com.promox.campaign.dto.CampaignResponse;
import com.promox.campaign.entity.Campaign;
import com.promox.campaign.service.CampaignService;
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
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campaign Management", description = "APIs for managing marketing campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @Operation(summary = "Create new campaign", description = "Create a new marketing campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CampaignRequest request) {
        log.info("REST request to create campaign: {}", request.getName());
        
        CampaignResponse response = campaignService.createCampaign(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Campaign created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by ID", description = "Retrieve campaign details by ID")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(@PathVariable Long id) {
        log.info("REST request to get campaign: {}", id);
        
        CampaignResponse response = campaignService.getCampaignById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all campaigns", description = "Retrieve all campaigns with pagination")
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> getAllCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        log.info("REST request to get all campaigns, page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CampaignResponse> campaigns = campaignService.getAllCampaigns(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update campaign", description = "Update existing campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request) {
        
        log.info("REST request to update campaign: {}", id);
        
        CampaignResponse response = campaignService.updateCampaign(id, request);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete campaign", description = "Delete/Cancel a campaign")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        log.info("REST request to delete campaign: {}", id);
        
        campaignService.deleteCampaign(id);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign cancelled successfully", null));
    }

    // Status management endpoints
    
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate campaign", description = "Activate a scheduled or paused campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> activateCampaign(@PathVariable Long id) {
        log.info("REST request to activate campaign: {}", id);
        
        CampaignResponse response = campaignService.activateCampaign(id);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign activated successfully", response));
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause campaign", description = "Pause an active campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> pauseCampaign(@PathVariable Long id) {
        log.info("REST request to pause campaign: {}", id);
        
        CampaignResponse response = campaignService.pauseCampaign(id);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign paused successfully", response));
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "Resume campaign", description = "Resume a paused campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> resumeCampaign(@PathVariable Long id) {
        log.info("REST request to resume campaign: {}", id);
        
        CampaignResponse response = campaignService.resumeCampaign(id);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign resumed successfully", response));
    }

    @PostMapping("/{id}/schedule")
    @Operation(summary = "Schedule campaign", description = "Schedule a draft campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> scheduleCampaign(@PathVariable Long id) {
        log.info("REST request to schedule campaign: {}", id);
        
        CampaignResponse response = campaignService.scheduleCampaign(id);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign scheduled successfully", response));
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "End campaign", description = "End a campaign manually")
    public ResponseEntity<ApiResponse<CampaignResponse>> endCampaign(@PathVariable Long id) {
        log.info("REST request to end campaign: {}", id);
        
        CampaignResponse response = campaignService.endCampaign(id);
        
        return ResponseEntity.ok(ApiResponse.success("Campaign ended successfully", response));
    }

    // Query endpoints
    
    @GetMapping("/active")
    @Operation(summary = "Get active campaigns", description = "Retrieve all currently active campaigns")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getActiveCampaigns() {
        log.info("REST request to get active campaigns");
        
        List<CampaignResponse> campaigns = campaignService.getActiveCampaigns();
        
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Get scheduled campaigns", description = "Retrieve all scheduled campaigns")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getScheduledCampaigns() {
        log.info("REST request to get scheduled campaigns");
        
        List<CampaignResponse> campaigns = campaignService.getScheduledCampaigns();
        
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get campaigns by status", description = "Retrieve campaigns by status")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getCampaignsByStatus(
            @PathVariable Campaign.CampaignStatus status) {
        
        log.info("REST request to get campaigns by status: {}", status);
        
        List<CampaignResponse> campaigns = campaignService.getCampaignsByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get campaigns by type", description = "Retrieve campaigns by type")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getCampaignsByType(
            @PathVariable Campaign.CampaignType type) {
        
        log.info("REST request to get campaigns by type: {}", type);
        
        List<CampaignResponse> campaigns = campaignService.getCampaignsByType(type);
        
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/search")
    @Operation(summary = "Search campaigns", description = "Search campaigns by keyword")
    public ResponseEntity<ApiResponse<Page<CampaignResponse>>> searchCampaigns(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("REST request to search campaigns with keyword: {}", keyword);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CampaignResponse> campaigns = campaignService.searchCampaigns(keyword, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }
}
