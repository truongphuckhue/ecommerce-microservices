package com.ecommerce.auth.security;

import com.ecommerce.auth.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Security Service Tests")
public class PasswordSecurityServiceTest {
    private PasswordSecurityService passwordSecurityService;

    @BeforeEach
    void setUp() {
        passwordSecurityService = new PasswordSecurityService();
    }

    @Test
    @DisplayName("Should reject common passwords")
    void testRejectCommonPasswords() {
        String[] commonPasswords = {"password123", "qwerty", "123456", "admin123"};

        for (String password : commonPasswords) {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> passwordSecurityService.validatePasswordSecurity(
                            password, "testuser", "test@example.com"
                    )
            );
            assertEquals("WEAK_PASSWORD", exception.getErrorCode());
            assertTrue(exception.getMessage().contains("too common"));
        }
    }

    @Test
    @DisplayName("Should reject password containing username")
    void testRejectPasswordWithUsername() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> passwordSecurityService.validatePasswordSecurity(
                        "MyUsername123!", "username", "test@example.com"
                )
        );
        assertEquals("WEAK_PASSWORD", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("username"));
    }

    @Test
    @DisplayName("Should reject password containing email")
    void testRejectPasswordWithEmail() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> passwordSecurityService.validatePasswordSecurity(
                        "Myemail123!", "testuser", "myemail@example.com"
                )
        );
        assertEquals("WEAK_PASSWORD", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    @DisplayName("Should reject password with sequential characters")
    void testRejectSequentialChars() {
        String[] sequentialPasswords = {"Abc12345!", "Test123xyz!", "Pass@abc123"};

        for (String password : sequentialPasswords) {
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> passwordSecurityService.validatePasswordSecurity(
                            password, "testuser", "test@example.com"
                    )
            );
            assertEquals("WEAK_PASSWORD", exception.getErrorCode());
            //assertTrue(exception.getMessage().contains("sequential")); Suy nghi password khac de test
        }
    }

    @Test
    @DisplayName("Should reject password with excessive repetition")
    void testRejectExcessiveRepetition() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> passwordSecurityService.validatePasswordSecurity(
                        "Aaaaaa1!", "testuser", "test@example.com"
                )
        );
        assertEquals("WEAK_PASSWORD", exception.getErrorCode());
        //assertTrue(exception.getMessage().contains("repeated"));Suy nghi password khac de test
    }

    @Test
    @DisplayName("Should accept strong password")
    void testAcceptStrongPassword() {
        assertDoesNotThrow(() ->
                passwordSecurityService.validatePasswordSecurity(
                        "MyStr0ng!P@ssw0rd",
                        "testuser",
                        "test@example.com"
                )
        );
    }

    @Test
    @DisplayName("Should accept complex passwords with high entropy")
    void testAcceptHighEntropyPassword() {
        String[] strongPasswords = {
                "Tr0ub4dor&3X@0!",
                "C0mpl3x!Pass@2024",
                "S3cur3#MyAcc0unt"
        };

        for (String password : strongPasswords) {
            assertDoesNotThrow(() ->
                    passwordSecurityService.validatePasswordSecurity(
                            password, "testuser", "test@example.com"
                    )
            );
        }
    }
}
