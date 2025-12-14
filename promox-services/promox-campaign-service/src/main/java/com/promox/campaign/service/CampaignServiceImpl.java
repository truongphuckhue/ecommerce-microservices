package com.promox.campaign.service;

import com.promox.campaign.dto.CampaignRequest;
import com.promox.campaign.dto.CampaignResponse;
import com.promox.campaign.entity.Campaign;
import com.promox.campaign.exception.CampaignNotFoundException;
import com.promox.campaign.mapper.CampaignMapper;
import com.promox.campaign.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;

    @Override
    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        log.info("Creating new campaign: {}", request.getName());

        // Check if name already exists
        if (campaignRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Campaign with name '" + request.getName() + "' already exists");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Campaign campaign = campaignMapper.toEntity(request);
        
        // Auto-schedule if start date is in future
        if (campaign.getStartDate().isAfter(LocalDateTime.now())) {
            campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
        }

        Campaign savedCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign created successfully with id: {}", savedCampaign.getId());
        
        return campaignMapper.toResponse(savedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        log.info("Fetching campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
        
        return campaignMapper.toResponse(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignResponse> getAllCampaigns(Pageable pageable) {
        log.info("Fetching all campaigns, page: {}, size: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        
        return campaignRepository.findAll(pageable)
                .map(campaignMapper::toResponse);
    }

    @Override
    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        log.info("Updating campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        // Check name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().equals(campaign.getName())) {
            if (campaignRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new IllegalArgumentException("Campaign with name '" + request.getName() + "' already exists");
            }
        }

        // Validate if campaign can be updated
        if (campaign.getStatus() == Campaign.CampaignStatus.ENDED) {
            throw new IllegalArgumentException("Cannot update ended campaign");
        }

        campaignMapper.updateEntityFromRequest(campaign, request);
        
        Campaign updatedCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign updated successfully: {}", updatedCampaign.getId());
        
        return campaignMapper.toResponse(updatedCampaign);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long id) {
        log.info("Deleting campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        // Soft delete: just change status
        campaign.setStatus(Campaign.CampaignStatus.CANCELLED);
        campaignRepository.save(campaign);
        
        log.info("Campaign cancelled successfully: {}", id);
    }

    @Override
    @Transactional
    public CampaignResponse activateCampaign(Long id) {
        log.info("Activating campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        // Validate status transition
        if (campaign.getStatus() != Campaign.CampaignStatus.SCHEDULED 
            && campaign.getStatus() != Campaign.CampaignStatus.PAUSED
            && campaign.getStatus() != Campaign.CampaignStatus.DRAFT) {
            throw new IllegalArgumentException(
                "Campaign can only be activated from SCHEDULED, PAUSED or DRAFT status");
        }

        // Check if dates are valid
        LocalDateTime now = LocalDateTime.now();
        if (campaign.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException("Cannot activate campaign before its start date");
        }
        if (campaign.getEndDate().isBefore(now)) {
            throw new IllegalArgumentException("Cannot activate expired campaign");
        }

        campaign.setStatus(Campaign.CampaignStatus.ACTIVE);
        Campaign activatedCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign activated successfully: {}", id);
        
        return campaignMapper.toResponse(activatedCampaign);
    }

    @Override
    @Transactional
    public CampaignResponse pauseCampaign(Long id) {
        log.info("Pausing campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        if (campaign.getStatus() != Campaign.CampaignStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active campaigns can be paused");
        }

        campaign.setStatus(Campaign.CampaignStatus.PAUSED);
        Campaign pausedCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign paused successfully: {}", id);
        
        return campaignMapper.toResponse(pausedCampaign);
    }

    @Override
    @Transactional
    public CampaignResponse resumeCampaign(Long id) {
        log.info("Resuming campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        if (campaign.getStatus() != Campaign.CampaignStatus.PAUSED) {
            throw new IllegalArgumentException("Only paused campaigns can be resumed");
        }

        campaign.setStatus(Campaign.CampaignStatus.ACTIVE);
        Campaign resumedCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign resumed successfully: {}", id);
        
        return campaignMapper.toResponse(resumedCampaign);
    }

    @Override
    @Transactional
    public CampaignResponse scheduleCampaign(Long id) {
        log.info("Scheduling campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft campaigns can be scheduled");
        }

        campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
        Campaign scheduledCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign scheduled successfully: {}", id);
        
        return campaignMapper.toResponse(scheduledCampaign);
    }

    @Override
    @Transactional
    public CampaignResponse endCampaign(Long id) {
        log.info("Ending campaign with id: {}", id);
        
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));

        campaign.setStatus(Campaign.CampaignStatus.ENDED);
        Campaign endedCampaign = campaignRepository.save(campaign);
        
        log.info("Campaign ended successfully: {}", id);
        
        return campaignMapper.toResponse(endedCampaign);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        log.info("Fetching active campaigns");
        
        return campaignRepository.findActiveCampaigns(LocalDateTime.now())
                .stream()
                .map(campaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getScheduledCampaigns() {
        log.info("Fetching scheduled campaigns");
        
        return campaignRepository.findScheduledCampaigns(LocalDateTime.now())
                .stream()
                .map(campaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByStatus(Campaign.CampaignStatus status) {
        log.info("Fetching campaigns by status: {}", status);
        
        return campaignRepository.findByStatus(status)
                .stream()
                .map(campaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getCampaignsByType(Campaign.CampaignType type) {
        log.info("Fetching campaigns by type: {}", type);
        
        return campaignRepository.findByType(type)
                .stream()
                .map(campaignMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CampaignResponse> searchCampaigns(String keyword, Pageable pageable) {
        log.info("Searching campaigns with keyword: {}", keyword);
        
        return campaignRepository.searchByName(keyword, pageable)
                .map(campaignMapper::toResponse);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void startScheduledCampaigns() {
        log.info("Running scheduled task to start campaigns");
        
        List<Campaign> campaignsToStart = campaignRepository
                .findCampaignsToStart(LocalDateTime.now());
        
        for (Campaign campaign : campaignsToStart) {
            campaign.setStatus(Campaign.CampaignStatus.ACTIVE);
            campaignRepository.save(campaign);
            log.info("Auto-started campaign: {} - {}", campaign.getId(), campaign.getName());
        }
        
        log.info("Started {} campaigns", campaignsToStart.size());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void endExpiredCampaigns() {
        log.info("Running scheduled task to end expired campaigns");
        
        List<Campaign> campaignsToEnd = campaignRepository
                .findCampaignsToEnd(LocalDateTime.now());
        
        for (Campaign campaign : campaignsToEnd) {
            campaign.setStatus(Campaign.CampaignStatus.ENDED);
            campaignRepository.save(campaign);
            log.info("Auto-ended campaign: {} - {}", campaign.getId(), campaign.getName());
        }
        
        log.info("Ended {} campaigns", campaignsToEnd.size());
    }
}
