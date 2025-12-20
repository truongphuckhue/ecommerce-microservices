package com.ecommerce.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InputSanitizer {
    /**
     * Sanitize text input by removing all HTML tags and special characters
     * Used for: firstName, lastName
     */
    public String sanitizeText(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String original = input;

        // Step 1: Remove all HTML tags
        String cleaned = Jsoup.clean(input, Safelist.none());
        cleaned = Jsoup.parse(cleaned).text(); //cover &=>amp trong html
        // Step 2: Remove special characters (keep letters, spaces, hyphens, apostrophes)
        cleaned = cleaned.replaceAll("[^a-zA-Z\\s\\-']", "");

        // Step 3: Normalize whitespace
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        if (!cleaned.equals(original)) {
            log.warn("Input was sanitized. Original length: {}, Cleaned length: {}",
                    original.length(), cleaned.length());
        }

        return cleaned;
    }

    /**
     * Sanitize username
     * Already validated by regex, but still clean HTML
     */
    public String sanitizeUsername(String username) {
        return sanitize(username);
    }

    /**
     * Sanitize email
     * Already validated by @Email, but still clean HTML
     */
    public String sanitizeEmail(String email) {
        return sanitize(email);
    }

    public String sanitizeUsernameOrEmail(String input) {
        return sanitize(input);
    }

    private String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        return Jsoup.clean(input, Safelist.none())
                .toLowerCase()
                .trim();
    }

}
