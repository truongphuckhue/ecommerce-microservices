package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u 
        LEFT JOIN FETCH u.roles 
        WHERE LOWER(u.username) = LOWER(:identifier) 
           OR LOWER(u.email) = LOWER(:identifier)
        """)
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    @Modifying
    @Query("""
        UPDATE User u 
        SET u.lastLogin = :loginTime, 
            u.failedLoginAttempts = 0,
            u.lockedUntil = null
        WHERE u.id = :userId
        """)
    void updateLoginSuccess(
            @Param("userId") Long userId,
            @Param("loginTime") LocalDateTime loginTime
    );

    @Modifying
    @Query("""
        UPDATE User u 
        SET u.failedLoginAttempts = u.failedLoginAttempts + 1 
        WHERE u.id = :userId
        """)
    void incrementFailedAttempts(@Param("userId") Long userId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    @Query("SELECT CASE " +
            "WHEN (SELECT COUNT(u) FROM User u WHERE u.username = :username) > 0 " +
            "     AND (SELECT COUNT(u) FROM User u WHERE u.email = :email) > 0 " +
            "     THEN 'both' " +
            "WHEN (SELECT COUNT(u) FROM User u WHERE u.username = :username) > 0 " +
            "     THEN 'username' " +
            "WHEN (SELECT COUNT(u) FROM User u WHERE u.email = :email) > 0 " +
            "     THEN 'email' " +
            "ELSE null END")
    String findExistingField(
            @Param("username") String username,
            @Param("email") String email
    );

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = true, u.lockedUntil = null, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil < :now")
    Optional<User> findLockedUsersToUnlock(@Param("now") LocalDateTime now);
}
