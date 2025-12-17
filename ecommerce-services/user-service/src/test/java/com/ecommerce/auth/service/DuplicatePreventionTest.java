package com.ecommerce.auth.service;

import com.ecommerce.auth.common.exception.BusinessException;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class DuplicatePreventionTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testPreventDuplicateUsername() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("duplicate_test")
                .email("test1@example.com")
                .password("SecurePass@123")
                .build();

        // When - First registration
        authService.register(request);

        // Then - Second registration should fail
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("duplicate_test")
                .email("test2@example.com")
                .password("SecurePass@123")
                .build();

        assertThrows(BusinessException.class, () -> {
            authService.register(duplicateRequest);
        });
    }

    @Test
    void testPreventDuplicateEmail() {
        // Similar test for email
        RegisterRequest request = RegisterRequest.builder()
                .username("user1")
                .email("duplicate@test.com")
                .password("SecurePass@123")
                .build();

        authService.register(request);

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("user2")
                .email("duplicate@test.com")
                .password("SecurePass@123")
                .build();

        assertThrows(BusinessException.class, () -> {
            authService.register(duplicateRequest);
        });
    }
}
