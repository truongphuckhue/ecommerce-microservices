package com.ecommerce.auth.service;

import com.ecommerce.auth.common.exception.InvalidTokenException;
import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.util.JwtUtil;
import com.ecommerce.auth.common.exception.BusinessException;
import com.ecommerce.auth.common.exception.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_EXISTS", "Username already exists");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("EMAIL_EXISTS", "Email already exists");
        }

        try{
            // Create user
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(Set.of("ROLE_USER"))
                    .failedLoginAttempts(0)
                    .build();

            user = userRepository.save(user); // ← Có thể duplicate nếu 2 requests cùng lúc -> đẩyvaofo trycatch
            log.info("User registered successfully: {}", user.getUsername());

            return UserResponse.fromUser(user);
        } catch (DataIntegrityViolationException e) {
            // Database constraint caught the duplicate
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("uk_username")) {
                    log.warn("Duplicate username caught by database constraint: {}", request.getUsername());
                    throw new BusinessException("USERNAME_EXISTS", "Username already exists");
                }
                if (message.contains("uk_email")) {
                    log.warn("Duplicate email caught by database constraint: {}", request.getEmail());
                    throw new BusinessException("EMAIL_EXISTS", "Email already exists");
                }
            }
            throw new BusinessException("DATABASE_ERROR", "Database constraint violation");
        }

    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsernameOrEmail());

        // Find user
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password")));

        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Account is locked: {}", user.getUsername());
            throw new LockedException("Account is locked until " + user.getLockedUntil());
        }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );

            // Reset failed attempts on successful login
            user.resetFailedLoginAttempts();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate tokens
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            // Save refresh token to database
            saveRefreshToken(user, refreshToken);

            log.info("User logged in successfully: {}", user.getUsername());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())  // ← FIX
                    .user(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(user.getRoles())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            // Increment failed login attempts
            user.incrementFailedLoginAttempts();
            
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount(LOCK_DURATION_MINUTES);
                userRepository.save(user);
                log.warn("Account locked due to failed login attempts: {}", user.getUsername());
                throw new LockedException("Account locked due to multiple failed login attempts");
            }
            
            userRepository.save(user);
            log.warn("Failed login attempt for user: {}", user.getUsername());
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();
        
        log.info("Refresh token request");

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new BusinessException("INVALID_TOKEN", "Invalid refresh token");
        }

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BusinessException("TOKEN_NOT_FOUND", "Refresh token not found"));

        // Check if token is expired or revoked
        if (refreshToken.isExpired() || refreshToken.getRevoked()) {
            throw new BusinessException("TOKEN_EXPIRED", "Refresh token is expired or revoked");
        }

        // Extract username and load user details
        String username = jwtUtil.getUsernameFromToken(refreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(userDetails.getUsername());

        log.info("Token refreshed successfully for user: {}", username);

        User user = refreshToken.getUser();
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationInSeconds())  // ← FIX: bỏ / 1000
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        log.info("Logout request received");

        // Extract token from Bearer scheme
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // Validate token format first
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid token provided for logout");
                throw new InvalidTokenException("Invalid token");
            }

            // Extract username và expiration từ token
            String username = jwtUtil.getUsernameFromToken(token);
            Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

            // Tính thời gian còn lại của token (milliseconds)
            long currentTimeMillis = System.currentTimeMillis();
            long expirationTimeMillis = expirationDate.getTime();
            long remainingTimeMillis = expirationTimeMillis - currentTimeMillis;

            // Chỉ blacklist nếu token chưa hết hạn
            if (remainingTimeMillis > 0) {
                redisTemplate.opsForValue().set(
                        TOKEN_BLACKLIST_PREFIX + token,
                        username,
                        remainingTimeMillis,
                        TimeUnit.MILLISECONDS
                );

                long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis);
                log.info("User '{}' logged out successfully. Token blacklisted for {} minutes ({} hours)",
                        username,
                        remainingMinutes,
                        String.format("%.2f", remainingMinutes / 60.0));
            } else {
                log.info("User '{}' logged out. Token already expired, skipping blacklist", username);
            }

        } catch (ExpiredJwtException e) {
            log.info("Logout attempted with expired token");
            // Token đã hết hạn, không cần blacklist
        } catch (JwtException e) {
            log.error("JWT error during logout: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token format");
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            throw new RuntimeException("Logout failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        return UserResponse.fromUser(user);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("All tokens revoked for user ID: {}", userId);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(
                        jwtUtil.getRefreshTokenExpirationInSeconds()))  // ← FIX: dùng method mới
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }
}
