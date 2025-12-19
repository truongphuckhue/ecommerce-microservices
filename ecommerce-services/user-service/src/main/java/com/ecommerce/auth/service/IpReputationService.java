package com.ecommerce.auth.service;

import com.ecommerce.auth.common.exception.BusinessException;
import com.ecommerce.auth.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpReputationService {
    private final RestTemplate restTemplate;
    private final AuditService auditService;

    @Value("${app.security.ip-check.enabled:false}")
    private boolean ipCheckEnabled;

    public void checkIpReputation(String ipAddress, String username) {
        if (!ipCheckEnabled) {
            return;
        }

        try {
            // Check if IP has suspicious activity in our audit logs
            List<AuditLog> suspiciousActivity = auditService
                    .getSuspiciousActivityByIp(ipAddress, 24);

            if (suspiciousActivity.size() >= 10) {
                log.warn("IP {} has {} suspicious activities",
                        ipAddress, suspiciousActivity.size());
                throw new BusinessException("SUSPICIOUS_IP",
                        "Your IP address has been flagged for suspicious activity. " +
                                "Please contact support.");
            }

            // Optional: Check external IP reputation service
            // Example: AbuseIPDB, IPQualityScore, etc.

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking IP reputation", e);
            // Don't block registration if IP check fails
        }
    }
}
