package org.example.firstpetprojectexchanger.service;

import java.time.Duration;

import org.example.firstpetprojectexchanger.config.LedgerProperties;
import org.example.firstpetprojectexchanger.dto.AuthResponse;
import org.example.firstpetprojectexchanger.dto.LoginRequest;
import org.example.firstpetprojectexchanger.dto.RegisterRequest;
import org.example.firstpetprojectexchanger.dto.UserDto;
import org.example.firstpetprojectexchanger.exception.BadRequestException;
import org.example.firstpetprojectexchanger.exception.ConflictException;
import org.example.firstpetprojectexchanger.exception.ResourceNotFoundException;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.repository.UserRepository;
import org.example.firstpetprojectexchanger.security.JwtService;
import org.example.firstpetprojectexchanger.security.RateLimitService;
import org.example.firstpetprojectexchanger.security.RefreshTokenReuseException;
import org.example.firstpetprojectexchanger.security.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_LOGINS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;
    private final LedgerProperties properties;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       RateLimitService rateLimitService,
                       LedgerProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(User.Role.USER);
        userRepository.save(user);
        log.info("New user registered: {}", email);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        rateLimitService.check("login:" + request.email().trim().toLowerCase(),
                properties.rateLimit().loginPerMinute(), Duration.ofMinutes(1));

        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

        if (user.getStatus() == User.Status.LOCKED) {
            throw new BadRequestException("Account is locked due to repeated failed login attempts");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }
        resetFailedLogins(user);
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        var record = refreshTokenService.validate(refreshToken);
        User user = userRepository.findById(record.userId())
                .orElseThrow(() -> new BadRequestException("User no longer exists"));
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }
        String rotated = refreshTokenService.rotate(refreshToken);
        return issueTokens(user, rotated);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            try {
                refreshTokenService.revoke(refreshToken);
            } catch (RefreshTokenReuseException ignored) {
               
            }
        }
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return UserDto.from(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGINS) {
            user.setStatus(User.Status.LOCKED);
            log.warn("User {} locked after {} failed attempts", user.getEmail(), user.getFailedLoginAttempts());
        }
        userRepository.save(user);
    }

    private void resetFailedLogins(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    private AuthResponse issueTokens(User user) {
        return issueTokens(user, refreshTokenService.issue(user.getId()));
    }

    private AuthResponse issueTokens(User user, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(user);
        return AuthResponse.of(accessToken, jwtService.getAccessTokenTtlSeconds(), refreshToken, UserDto.from(user));
    }
}
/*
AuthService

-userRepository: UserRepository
-passwordEncoder: PasswordEncoder
-jwtService: JwtService
-refreshTokenService: RefreshTokenService
-rateLimitService: RateLimitService
-properties: LedgerProperties
-MAX_FAILED_LOGINS: int = 5

+register(RegisterRequest): AuthResponse
+login(LoginRequest): AuthResponse
+refresh(String): AuthResponse
+logout(String): void
+me(Long): UserDto

-handleFailedLogin(User): void
-resetFailedLogins(User): void
-issueTokens(User): AuthResponse
-issueTokens(User, String): AuthResponse
 */