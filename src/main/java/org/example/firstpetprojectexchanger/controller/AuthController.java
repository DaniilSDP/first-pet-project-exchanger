package org.example.firstpetprojectexchanger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auths")
public class AuthController {
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