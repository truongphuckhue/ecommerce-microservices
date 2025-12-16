package com.promox.campaign.service;

import com.promox.campaign.dto.CampaignRequest;
import com.promox.campaign.dto.CampaignResponse;
import com.promox.campaign.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CampaignService {

    // CRUD operations
    CampaignResponse createCampaign(CampaignRequest request);
    
    CampaignResponse getCampaignById(Long id);
    
    Page<CampaignResponse> getAllCampaigns(Pageable pageable);
    
    CampaignResponse updateCampaign(Long id, CampaignRequest request);
    
    void deleteCampaign(Long id);

    // Status management
    CampaignResponse activateCampaign(Long id);
    
    CampaignResponse pauseCampaign(Long id);
    
    CampaignResponse resumeCampaign(Long id);
    
    CampaignResponse scheduleCampaign(Long id);
    
    CampaignResponse endCampaign(Long id);

    // Query operations
    List<CampaignResponse> getActiveCampaigns();
    
    List<CampaignResponse> getScheduledCampaigns();
    
    List<CampaignResponse> getCampaignsByStatus(Campaign.CampaignStatus status);
    
    List<CampaignResponse> getCampaignsByType(Campaign.CampaignType type);
    
    Page<CampaignResponse> searchCampaigns(String keyword, Pageable pageable);

    // Scheduled tasks
    void startScheduledCampaigns();
    
    void endExpiredCampaigns();
}
