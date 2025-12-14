package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * Email sending service using JavaMailSender
 * Supports both simple text emails and HTML emails with templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    private static final String FROM_EMAIL = "noreply@ecommerce.com";

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            log.info("Sending simple email to: {}, subject: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("Sending HTML email to: {}, subject: {}, template: {}", to, subject, templateName);

            // Process template with variables
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);

            // Create MIME message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);

            log.info("HTML email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to create email message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send email with retry on failure
     */
    public void sendEmailWithRetry(String to, String subject, String text, int maxRetries) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                sendSimpleEmail(to, subject, text);
                return; // Success
            } catch (Exception e) {
                lastException = e;
                attempt++;
                log.warn("Email send attempt {} failed for: {}", attempt, to);

                if (attempt < maxRetries) {
                    try {
                        // Exponential backoff
                        Thread.sleep(1000L * (long) Math.pow(2, attempt - 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Email send interrupted", ie);
                    }
                }
            }
        }

        log.error("Failed to send email after {} attempts to: {}", maxRetries, to);
        throw new RuntimeException("Failed to send email after " + maxRetries + " attempts", lastException);
    }
}
