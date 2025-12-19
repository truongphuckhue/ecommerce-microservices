package com.ecommerce.auth.service;

import com.ecommerce.auth.entity.AuditLog;
import com.ecommerce.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    /**
     * Log audit event asynchronously
     */
    @Async("auditExecutor")
    public void logAsync(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: action={}, status={}, user={}",
                    auditLog.getAction(), auditLog.getStatus(), auditLog.getUsername());
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
            // Don't throw - audit logging failure shouldn't break business logic
        }
    }

    /**
     * Log audit event synchronously
     */
    public void log(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    /**
     * Get suspicious activity by IP
     */
    public List<AuditLog> getSuspiciousActivityByIp(String ipAddress, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findFailedAttemptsByIpSince(ipAddress, since);
    }

    /**
     * Analyze registration patterns for suspicious activity
     */
    public boolean isSuspiciousRegistrationPattern(String ipAddress) {
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        List<AuditLog> recentAttempts = auditLogRepository
                .findByActionAndStatusSince("REGISTER", "FAILURE", lastHour);

        long failedFromIp = recentAttempts.stream()
                .filter(log -> ipAddress.equals(log.getIpAddress()))
                .count();

        return failedFromIp >= 5; // 5 failed attempts in 1 hour = suspicious
    }
}
