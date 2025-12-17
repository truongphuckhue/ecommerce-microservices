package com.ecommerce.auth.security;

import com.ecommerce.auth.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class PasswordSecurityService {
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password123", "123456", "123456789", "12345678",
            "qwerty", "abc123", "monkey", "1234567", "letmein",
            "trustno1", "dragon", "baseball", "iloveyou", "master",
            "sunshine", "ashley", "bailey", "passw0rd", "shadow",
            "123123", "654321", "superman", "qazwsx", "michael",
            "football", "admin", "admin123", "root", "toor"
    );

    /**
     * Validate password security
     * Checks:
     * 1. Not a common password
     * 2. Doesn't contain username
     * 3. Doesn't contain email local part
     * 4. Has sufficient entropy
     * 5. No sequential characters
     * 6. Not too repetitive
     */
    public void validatePasswordSecurity(String password, String username, String email) {
        log.debug("Validating password security for user: {}", username);

        // 1. Check common passwords
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new BusinessException("WEAK_PASSWORD",
                    "This password is too common. Please choose a stronger password.");
        }

        // 2. Password must not contain username
        if (password.toLowerCase().contains(username.toLowerCase())) {
            throw new BusinessException("WEAK_PASSWORD",
                    "Password cannot contain your username.");
        }

        // 3. Password must not contain email local part
        String emailLocal = email.substring(0, email.indexOf('@'));
        if (password.toLowerCase().contains(emailLocal.toLowerCase())) {
            throw new BusinessException("WEAK_PASSWORD",
                    "Password cannot contain your email.");
        }

        // 4. Check entropy (randomness)
        double entropy = calculateEntropy(password);
        if (entropy < 50) {
            throw new BusinessException("WEAK_PASSWORD",
                    String.format("Password is not strong enough (entropy: %.2f). Use a mix of different character types.", entropy));
        }

        // 5. Check for sequential characters (abc, 123, xyz)
        if (hasSequentialChars(password)) {
            throw new BusinessException("WEAK_PASSWORD",
                    "Password contains sequential characters (like abc or 123). Please avoid patterns.");
        }

        // 6. Check for excessive repetition
        if (hasExcessiveRepetition(password)) {
            throw new BusinessException("WEAK_PASSWORD",
                    "Password has too many repeated characters. Please use more variety.");
        }

        log.debug("Password security validation passed for user: {}", username);
    }

    /**
     * Calculate Shannon entropy
     * Higher entropy = more random = stronger password
     */
    private double calculateEntropy(String password) {
        Map<Character, Integer> frequencyMap = new HashMap<>();

        for (char c : password.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        double entropy = 0.0;
        int length = password.length();

        for (int frequency : frequencyMap.values()) {
            double probability = (double) frequency / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy * length;
    }

    /**
     * Check for sequential characters like "abc", "123", "xyz"
     */
    private boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();

        for (int i = 0; i < lower.length() - 2; i++) {
            char c1 = lower.charAt(i);
            char c2 = lower.charAt(i + 1);
            char c3 = lower.charAt(i + 2);

            // Check ascending sequence
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }

            // Check descending sequence
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if password has too many repeated characters
     * More than 50% same character = too repetitive
     */
    private boolean hasExcessiveRepetition(String password) {
        Map<Character, Integer> frequencyMap = new HashMap<>();

        for (char c : password.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        int maxFrequency = frequencyMap.values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        return maxFrequency > password.length() / 2;
    }
}
