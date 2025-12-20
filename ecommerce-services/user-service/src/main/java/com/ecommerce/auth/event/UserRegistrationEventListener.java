package com.ecommerce.auth.event;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.service.EmailService;
import com.ecommerce.auth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationEventListener {
    private final EmailVerificationService verificationService;
    private final EmailService emailService;

    @Async("registrationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        log.info("Handling user registration event for: {}", user.getUsername());

        try {
            // Send verification email
            verificationService.createAndSendVerificationToken(user);

            // Send welcome email (optional, but good UX)
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

            // You can add more async operations here:
            // - Update analytics
            // - Send to data warehouse
            // - Trigger marketing automation
            // - etc.

        } catch (Exception e) {
            log.error("Error handling user registration event", e);
            // Don't throw - this is async, throwing won't help
            // Consider adding to dead-letter queue for retry
        }
    }
}
