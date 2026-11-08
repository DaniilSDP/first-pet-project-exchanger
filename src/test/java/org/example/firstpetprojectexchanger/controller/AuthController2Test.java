package org.example.firstpetprojectexchanger.controller;

import org.example.firstpetprojectexchanger.dto.AuthResponse;
import org.example.firstpetprojectexchanger.dto.LoginRequest;
import org.example.firstpetprojectexchanger.dto.RefreshRequest;
import org.example.firstpetprojectexchanger.dto.RegisterRequest;
import org.example.firstpetprojectexchanger.dto.UserDto;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthController2Test {

    private AuthService authService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AuthResponse authResponse() {
        return new AuthResponse(
                "access-token",
                "Bearer",
                3600L,
                "refresh-token",
                null
        );
    }

    @Test
    @DisplayName("POST /api/auth/register -> 201")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Ivan", "Ivanov", "ivan@test.ru", "Ivanov123"
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).register(any(RegisterRequest.class));
    }

    
    
    @Test
    @DisplayName("POST /api/auth/login -> 200")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest(
                "vlad@mail.ru", "A5536dxVSCsc"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh -> 200")
    void refresh_Success() throws Exception {
        RefreshRequest request = new RefreshRequest("123321");

        when(authService.refresh("123321"))
                .thenReturn(authResponse());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refresh("123321");
    }

    @Test
    @DisplayName("POST /api/auth/logout -> 204")
    void logout_Success() throws Exception {
        RefreshRequest request = new RefreshRequest("123321");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).logout("123321");
    }

    @Test
    @DisplayName("GET /api/auth/me -> 200")
    void me_Success() throws Exception {
        User user = new User();
        user.setId(1L);

        var authentication = new UsernamePasswordAuthenticationToken(
                user, null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDto response = new UserDto(
                1L,
                "vlad@test.ru",
                "Vlad",
                "Ivanov",
                "USER",
                "ACTIVE",
                Instant.now()
        );

        when(authService.me(1L)).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("vlad@test.ru"))
                .andExpect(jsonPath("$.firstName").value("Vlad"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"));

        verify(authService).me(1L);
    }
}
