package com.promox.campaign.exception;

public class CampaignNotFoundException extends RuntimeException {
    public CampaignNotFoundException(String message) {
        super(message);
    }

    public CampaignNotFoundException(Long id) {
        super("Campaign not found with id: " + id);
    }
}
