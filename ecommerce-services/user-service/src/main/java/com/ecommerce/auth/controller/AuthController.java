package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - Register new user: {}", request.getUsername());
        
        UserResponse response = authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - User login attempt: {}", request.getUsernameOrEmail());
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /auth/refresh - Refresh token request");
        
        AuthResponse response = authService.refreshToken(request);
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        log.info("POST /auth/logout - User logout");
        
        authService.logout(token);
        
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("GET /auth/me - Get current user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        UserResponse response = authService.getCurrentUser(username);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/revoke-tokens/{userId}")
    public ResponseEntity<ApiResponse<Void>> revokeAllUserTokens(@PathVariable Long userId) {
        log.info("POST /auth/revoke-tokens/{} - Revoke all tokens for user", userId);
        
        authService.revokeAllUserTokens(userId);
        
        return ResponseEntity.ok(ApiResponse.success("All tokens revoked successfully", null));
    }
}
