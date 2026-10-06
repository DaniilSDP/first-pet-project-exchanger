package org.example.firstpetprojectexchanger.controller;

import org.example.firstpetprojectexchanger.dto.AuthResponse;
import org.example.firstpetprojectexchanger.dto.LoginRequest;
import org.example.firstpetprojectexchanger.dto.RefreshRequest;
import org.example.firstpetprojectexchanger.dto.RegisterRequest;
import org.example.firstpetprojectexchanger.dto.UserDto;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
//@Tag(name = "Auth", description = "Registration, login, token refresh and logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    //@Operation(summary = "Register a new user and return tokens")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    //@Operation(summary = "Log in with email/password and return tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    //@Operation(summary = "Rotate a refresh token and issue a new access token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    //@Operation(summary = "Revoke the refresh token")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    //              @Operation(summary = "Current authenticated user")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.me(user.getId()));
    }
}

/*
AuthController
-authService: AuthService

+register(RegisterRequest): ResponseEntity<AuthResponse>
+login(LoginRequest): ResponseEntity<AuthResponse>
+refresh(RefreshRequest): ResponseEntity<AuthResponse>
+logout(RefreshRequest): ResponseEntity<Void>
+me(User): ResponseEntity<UserDto>

 */