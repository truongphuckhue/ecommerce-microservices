package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user_id", columnList = "userId"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_created_at", columnList = "createdAt"),
                @Index(name = "idx_audit_ip", columnList = "ipAddress")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 50)
    private String action; // REGISTER, LOGIN, LOGOUT, PASSWORD_CHANGE, etc.

    @Column(nullable = false, length = 50)
    private String status; // SUCCESS, FAILURE, BLOCKED

    @Column(length = 500)
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(length = 100)
    private String location; // Country, City from IP

    @Column(length = 50)
    private String errorCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Factory methods
    public static AuditLog registerSuccess(User user, String ipAddress, String userAgent) {
        return AuditLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .action("REGISTER")
                .status("SUCCESS")
                .details("User registered successfully")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    public static AuditLog registerFailure(String username, String reason,
                                           String ipAddress, String userAgent) {
        return AuditLog.builder()
                .username(username != null ? username : "unknown")
                .action("REGISTER")
                .status("FAILURE")
                .details(reason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
    }

    public static AuditLog rateLimitExceeded(String username, String action,
                                             String ipAddress) {
        return AuditLog.builder()
                .username(username != null ? username : "unknown")
                .action(action)
                .status("BLOCKED")
                .details("Rate limit exceeded")
                .errorCode("RATE_LIMIT_EXCEEDED")
                .ipAddress(ipAddress)
                .build();
    }
}
