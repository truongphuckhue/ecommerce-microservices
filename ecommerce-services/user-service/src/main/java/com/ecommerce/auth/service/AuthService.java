package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.*;

public interface AuthService {
    
    UserResponse register(RegisterRequest request) throws InterruptedException;
    
    AuthResponse login(LoginRequest request);
    
    AuthResponse refreshToken(RefreshTokenRequest request);
    
    void logout(String token);
    
    UserResponse getCurrentUser(String username);
    
    void revokeAllUserTokens(Long userId);
}
