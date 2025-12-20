package com.ecommerce.auth.service;

import com.ecommerce.auth.common.exception.InvalidTokenException;
import com.ecommerce.auth.common.exception.RateLimitExceededException;
import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.AuditLog;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.event.UserRegisteredEvent;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.InputSanitizer;
import com.ecommerce.auth.security.PasswordSecurityService;
import com.ecommerce.auth.util.JwtUtil;
import com.ecommerce.auth.common.exception.BusinessException;
import com.ecommerce.auth.common.exception.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

    private final PasswordSecurityService passwordSecurityService;
    private final InputSanitizer inputSanitizer;
    private final RateLimitService rateLimitService;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;
    private final ApplicationEventPublisher eventPublisher;

    private final EmailVerificationService verificationService;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) throws InterruptedException {
        log.info("Registering new user: {}", request.getUsername());

        // STEP 0: Rate limiting check (FIRST!)
        String clientIp = getClientIp();
        String userAgent = getUserAgent();

        try {
            rateLimitService.checkRateLimit(
                    "register:ip:" + clientIp,
                    5,      // 5 attempts
                    3600,   // per hour
                    "Too many registration attempts from this IP address"
            );

            // Also rate limit by email
            rateLimitService.checkRateLimit(
                    "register:email:" + request.getEmail(),
                    3,      // 3 attempts
                    86400,  // per day
                    "Too many registration attempts with this email"
            );
        } catch (RateLimitExceededException e) {
            // ← Log rate limit exceeded
            auditService.logAsync(AuditLog.rateLimitExceeded(
                    request.getUsername(), "REGISTER", clientIp));
            throw e;
        }


        // STEP 1: Sanitize all inputs FIRST
        String username = inputSanitizer.sanitizeUsername(request.getUsername());
        String email = inputSanitizer.sanitizeEmail(request.getEmail());
        String firstName = inputSanitizer.sanitizeText(request.getFirstName());
        String lastName = inputSanitizer.sanitizeText(request.getLastName());

        // STEP 2: Password security validation
        passwordSecurityService.validatePasswordSecurity(
                request.getPassword(),
                request.getUsername(),
                request.getEmail()
        );

        // STEP 3: Check duplicates

        String existingField = userRepository.findExistingField(username, email);

        if (existingField != null) {
            String errorMessage = switch (existingField) {
                case "username" -> "Username already exists";
                case "email" -> "Email already exists";
                case "both" -> "Username and email already exist";
                default -> "Registration failed";
            };

            auditService.logAsync(AuditLog.registerFailure(
                    username, errorMessage, clientIp, userAgent));

            throw new BusinessException("REGISTRATION_FAILED",
                    "Registration failed. Please check your information.");
        }

        try{
            // STEP 4: Create user with SANITIZED values
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(firstName)
                    .lastName(lastName)
                    .phoneNumber(request.getPhoneNumber())
                    .enabled(false) //Disabled until email verified
                    .emailVerified(false)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .roles(Set.of("ROLE_USER"))
                    .failedLoginAttempts(0)
                    .build();

            user = userRepository.save(user);
            log.info("User registered successfully: {}", user.getUsername());

            // STEP 5: Send verification email (async)
            eventPublisher.publishEvent(new UserRegisteredEvent(
                    this, user, clientIp, userAgent));

            auditService.logAsync(AuditLog.registerSuccess(
                    user, clientIp, userAgent));

            return UserResponse.fromUser(user);
        } catch (RateLimitExceededException e) {
            auditService.logAsync(AuditLog.rateLimitExceeded(
                    request.getUsername(), "REGISTER", clientIp));
            throw e;
        } catch (DataIntegrityViolationException e) {
            auditService.logAsync(AuditLog.registerFailure(
                    request.getUsername(), "Database constraint violation",
                    clientIp, userAgent));
            handleDatabaseConstraintViolation(e, request);
            throw new BusinessException("DATABASE_ERROR", "Registration failed");
        } catch (Exception e) {
            auditService.logAsync(AuditLog.registerFailure(
                    request.getUsername(), "Unexpected error: " + e.getMessage(),
                    clientIp, userAgent));
            log.error("Unexpected error during registration", e);
            throw new BusinessException("REGISTRATION_ERROR",
                    "Registration failed. Please try again.");
        }

    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // STEP 0: Rate limiting check (BEFORE anything else)
        String clientIp = getClientIp();
        String userAgent = getUserAgent();

        // STEP 1: Sanitize inputs FIRST
        String sanitizedIdentifier = inputSanitizer.sanitizeUsernameOrEmail(
                request.getUsernameOrEmail()
        );

        try {
            // IP-based rate limiting: 10 attempts per hour
            rateLimitService.checkRateLimit(
                    "login:ip:" + clientIp,
                    10,
                    3600,
                    "Too many login attempts from this IP address"
            );

            // User-based rate limiting: 5 attempts per 15 minutes
            rateLimitService.checkRateLimit(
                    "login:user:" + sanitizedIdentifier,
                    5,
                    900,
                    "Too many login attempts for this account"
            );

        } catch (RateLimitExceededException e) {
            // Log security event
            auditService.logAsync(AuditLog.rateLimitExceeded(
                    sanitizedIdentifier, "LOGIN", clientIp));
            throw e;
        }

        log.info("User login attempt - identifier length: {}",
                sanitizedIdentifier.length());

        // Find user
        User user = userRepository.findByUsernameOrEmail(sanitizedIdentifier)
                .orElseThrow(() -> new BusinessException(
                        "AUTHENTICATION_FAILED",
                        "Invalid username/email or password"  // Generic message
                ));

        // Check if account is locked
        if (user.isAccountLocked()) {
            auditService.logAsync(AuditLog.accountLocked(sanitizedIdentifier, clientIp, userAgent));
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

            auditService.logAsync(AuditLog.loginSuccess(user, clientIp, userAgent));
            rateLimitService.resetRateLimit("login:user:" + sanitizedIdentifier);

            // Generate tokens
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            // Save refresh token to database
            saveRefreshToken(user, refreshToken);

            userRepository.updateLoginSuccess(user.getId(), LocalDateTime.now());

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

            auditService.logAsync(AuditLog.loginFailure(
                    sanitizedIdentifier, "Invalid credentials", clientIp, userAgent
            ));

            // Increment failed login attempts
            user.incrementFailedLoginAttempts();
            
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {

                auditService.logAsync(AuditLog.accountLocked(
                        sanitizedIdentifier, clientIp, userAgent
                ));

                user.lockAccount(LOCK_DURATION_MINUTES);
                userRepository.save(user);
                log.warn("Account locked due to failed login attempts: {}", user.getUsername());
                throw new LockedException("Account locked due to multiple failed login attempts");
            }
            
            userRepository.save(user);
            auditService.logAsync(AuditLog.loginFailure(user.getUsername(), "Failed login attempt", clientIp, userAgent ));
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

    /**
     * Extract client IP from request
     * Handles X-Forwarded-For header for proxied requests
     */
    private String getClientIp() {
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For: client, proxy1, proxy2
            return xForwardedFor.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }

    private String getUserAgent() {
        return httpRequest.getHeader("User-Agent");
    }

    private void handleDatabaseConstraintViolation(
            DataIntegrityViolationException e, RegisterRequest request) {
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("uk_username")) {
                log.warn("Duplicate username caught by database: {}", request.getUsername());
                throw new BusinessException("USERNAME_EXISTS", "Username already exists");
            }
            if (message.contains("uk_email")) {
                log.warn("Duplicate email caught by database: {}", request.getEmail());
                throw new BusinessException("EMAIL_EXISTS", "Email already exists");
            }
        }
    }
}
