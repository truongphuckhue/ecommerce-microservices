package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    List<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    @Query("SELECT a FROM AuditLog a WHERE a.action = :action " +
            "AND a.status = :status AND a.createdAt >= :since")
    List<AuditLog> findByActionAndStatusSince(
            @Param("action") String action,
            @Param("status") String status,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ip " +
            "AND a.status = 'FAILURE' AND a.createdAt >= :since")
    List<AuditLog> findFailedAttemptsByIpSince(
            @Param("ip") String ip,
            @Param("since") LocalDateTime since
    );
}
