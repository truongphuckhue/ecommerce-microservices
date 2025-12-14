package com.promox.coupon.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class CouponCodeGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a single unique coupon code
     * Format: PREFIX-RANDOMCODE
     * Example: SUMMER-X7K9M2P4
     */
    public String generateCode(String prefix, int length) {
        StringBuilder code = new StringBuilder();
        
        if (prefix != null && !prefix.isEmpty()) {
            code.append(prefix.toUpperCase()).append("-");
        }
        
        int remainingLength = length - (prefix != null ? prefix.length() + 1 : 0);
        
        for (int i = 0; i < remainingLength; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            code.append(ALPHANUMERIC.charAt(index));
        }
        
        return code.toString();
    }

    /**
     * Generate multiple unique coupon codes
     * Ensures no duplicates in the generated set
     */
    public Set<String> generateUniqueCodes(String prefix, int length, int quantity) {
        Set<String> codes = new HashSet<>();
        
        int maxAttempts = quantity * 10; // Prevent infinite loop
        int attempts = 0;
        
        while (codes.size() < quantity && attempts < maxAttempts) {
            String code = generateCode(prefix, length);
            codes.add(code);
            attempts++;
        }
        
        if (codes.size() < quantity) {
            log.warn("Could only generate {} unique codes out of {} requested", codes.size(), quantity);
        }
        
        return codes;
    }

    /**
     * Generate a simple sequential code
     * Format: PREFIX-0001, PREFIX-0002, etc.
     */
    public String generateSequentialCode(String prefix, int sequenceNumber, int padding) {
        String paddedNumber = String.format("%0" + padding + "d", sequenceNumber);
        return prefix.toUpperCase() + "-" + paddedNumber;
    }

    /**
     * Validate coupon code format
     */
    public boolean isValidFormat(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        // Must be uppercase, alphanumeric, and hyphens only
        return code.matches("^[A-Z0-9-]+$");
    }
}
