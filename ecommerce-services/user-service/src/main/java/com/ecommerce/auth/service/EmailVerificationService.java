package com.ecommerce.auth.service;

import com.ecommerce.auth.common.exception.BusinessException;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.VerificationToken;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.verification.token-expiry-hours:24}")
    private int tokenExpiryHours;

    @Value("${app.verification.base-url}")
    private String baseUrl;

    /**
     * Create and send verification token
     */
    @Transactional
    public void createAndSendVerificationToken(User user) {
        log.info("Creating verification token for user: {}", user.getUsername());

        // Delete any existing unverified tokens
        tokenRepository.deleteByUserId(user.getId());

        // Create new token
        VerificationToken token = VerificationToken.create(user, tokenExpiryHours);
        tokenRepository.save(token);

        // Send email asynchronously
        String verificationUrl = buildVerificationUrl(token.getToken());
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationUrl);

        log.info("Verification token created and email sent for user: {}", user.getUsername());
    }

    /**
     * Verify email token
     */
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email token");

        // Find token
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid verification token"));

        // Check if already verified
        if (verificationToken.getVerified()) {
            log.warn("Token already verified: {}", token);
            throw new BusinessException("TOKEN_ALREADY_USED", "This token has already been used");
        }

        // Check if expired
        if (verificationToken.isExpired()) {
            log.warn("Token expired: {}", token);
            throw new BusinessException("TOKEN_EXPIRED", "Verification token has expired");
        }

        // Mark token as verified
        verificationToken.verify();
        tokenRepository.save(verificationToken);

        // Enable user account
        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setEmailVerified(true);  // Add this field to User entity
        user.setEmailVerifiedAt(LocalDateTime.now());  // Add this field to User entity
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getUsername());
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resending verification email to: {}", email);

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        // Check if already verified
        if (user.getEnabled() && user.getEmailVerified()) {
            throw new BusinessException("ALREADY_VERIFIED", "Email already verified");
        }

        // Check if there's an existing valid token
        tokenRepository.findByUserIdAndVerifiedFalse(user.getId())
                .ifPresent(token -> {
                    if (!token.isExpired()) {
                        throw new BusinessException("TOKEN_ALREADY_SENT",
                                "Verification email already sent. Please check your inbox.");
                    }
                });

        // Create and send new token
        createAndSendVerificationToken(user);
    }

    /**
     * Clean up expired tokens (scheduled job)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        log.info("Cleaning up expired verification tokens");
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Deleted {} expired tokens", deleted);
        return deleted;
    }

    private String buildVerificationUrl(String token) {
        return String.format("%s/api/auth/verify-email?token=%s", baseUrl, token);
    }
}
