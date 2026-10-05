package org.example.firstpetprojectexchanger.service;

public class AuthService {
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