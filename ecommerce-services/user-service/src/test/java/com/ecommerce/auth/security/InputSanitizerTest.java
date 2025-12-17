package com.ecommerce.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Input Sanitizer Tests")
public class InputSanitizerTest {
    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    @DisplayName("Should remove script tags from text")
    void testRemoveScriptTags() {
        String malicious = "<script>alert('XSS')</script>John";
        String sanitized = inputSanitizer.sanitizeText(malicious);

        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("</script>"));
        assertEquals("John", sanitized);
    }

    @Test
    @DisplayName("Should remove HTML tags from text")
    void testRemoveHtmlTags() {
        String html = "<b>Bold</b> and <i>italic</i> text";
        String sanitized = inputSanitizer.sanitizeText(html);

        assertFalse(sanitized.contains("<b>"));
        assertFalse(sanitized.contains("<i>"));
        assertEquals("Bold and italic text", sanitized);
    }

    @Test
    @DisplayName("Should remove special characters but keep valid ones")
    void testRemoveSpecialChars() {
        String input = "John@#$%Doe!&*()";
        String sanitized = inputSanitizer.sanitizeText(input);

        assertEquals("JohnDoe", sanitized);
    }

    @Test
    @DisplayName("Should keep hyphens and apostrophes in names")
    void testKeepValidCharacters() {
        String input = "Mary-Jane O'Connor";
        String sanitized = inputSanitizer.sanitizeText(input);

        assertEquals("Mary-Jane O'Connor", sanitized);
    }

    @Test
    @DisplayName("Should sanitize username to lowercase")
    void testSanitizeUsername() {
        String username = "TestUser<script>alert('xss')</script>";
        String sanitized = inputSanitizer.sanitizeUsername(username);

        assertFalse(sanitized.contains("<script>"));
        assertEquals(sanitized, sanitized.toLowerCase());
        assertTrue(sanitized.contains("testuser"));
    }

    @Test
    @DisplayName("Should sanitize email to lowercase")
    void testSanitizeEmail() {
        String email = "Test@Example.COM<script>";
        String sanitized = inputSanitizer.sanitizeEmail(email);

        assertFalse(sanitized.contains("<script>"));
        assertEquals("test@example.com", sanitized.toLowerCase());
    }

    @Test
    @DisplayName("Should handle null input")
    void testHandleNull() {
        assertNull(inputSanitizer.sanitizeText(null));
        assertNull(inputSanitizer.sanitizeUsername(null));
        assertNull(inputSanitizer.sanitizeEmail(null));
    }

    @Test
    @DisplayName("Should handle empty input")
    void testHandleEmpty() {
        assertEquals("", inputSanitizer.sanitizeText(""));
        assertEquals("", inputSanitizer.sanitizeUsername(""));
        assertEquals("", inputSanitizer.sanitizeEmail(""));
    }

    @Test
    @DisplayName("Should normalize whitespace")
    void testNormalizeWhitespace() {
        String input = "John    Doe  \t\n  Test";
        String sanitized = inputSanitizer.sanitizeText(input);

        assertEquals("John Doe Test", sanitized);
    }
}
