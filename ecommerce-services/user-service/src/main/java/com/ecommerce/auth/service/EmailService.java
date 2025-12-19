package com.ecommerce.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    /**
     * Send verification email asynchronously
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendVerificationEmail(String to, String username, String verificationUrl) {
        log.info("Sending verification email to: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set email details
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Email - " + appName);

            // Create context for template
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationUrl", verificationUrl);
            context.setVariable("appName", appName);

            // Process template
            String htmlContent = templateEngine.process("email/verification", context);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", to);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
            // Don't throw exception - email failure shouldn't break registration
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send welcome email asynchronously
     */
    @Async("emailExecutor")
    public CompletableFuture<Void> sendWelcomeEmail(String to, String username) {
        log.info("Sending welcome email to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to " + appName);
            message.setText(String.format(
                    "Hi %s,\n\nWelcome to %s! We're excited to have you on board.\n\nBest regards,\nThe %s Team",
                    username, appName, appName
            ));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
