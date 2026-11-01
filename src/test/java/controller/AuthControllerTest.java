package controller;


import org.example.firstpetprojectexchanger.controller.AuthController;
import org.example.firstpetprojectexchanger.dto.*;
import org.example.firstpetprojectexchanger.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void register_Success() throws Exception {

        // Arrange
        RegisterRequest request = new RegisterRequest(
                "test@test.ru",
                "passwd",
                "Ivan",
                "Ivanov"
        );

        AuthResponse response = new AuthResponse(
                "access-token",
                "Bearer",
                3600L,
                "refresh-token",
                null
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        // Verify
        verify(authService, times(1))
                .register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Успешная авторизация пользователя")
    void login_Success() throws Exception {

        // Arrange
        LoginRequest request = new LoginRequest(
                "vlad@mail.ru",
                "A5536dxVSCsc"
        );

        AuthResponse response = new AuthResponse(
                "access-token",
                "Bearer",
                3600L,
                "refresh-token",
                null
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        // Verify
        verify(authService, times(1))
                .login(any(LoginRequest.class));
    }


    @Test
    @DisplayName("Успешное обновление пользователя")
    void refresh_Success() throws Exception {

        // Arrange
        RefreshRequest request = new RefreshRequest(
                "123321"
        );

        AuthResponse response = new AuthResponse(
                "access-token",
                "Bearer",
                3600L,
                "refresh-token",
                null
        );

        when(authService.refresh(any(String.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        // Verify
        verify(authService, times(1))
                .refresh(any(String.class));
    }


    @Test
    @DisplayName("Успешный выход пользователя")
    void logout_Success() throws Exception {

        // Arrange
        RefreshRequest request = new RefreshRequest(
                "123321"
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Verify
        verify(authService, times(1))
                .logout("123321");
    }


    @Test
    @DisplayName("Успешное получение текущего пользователя")
    void me_Success() throws Exception {

        // Arrange
        UserDto response = new UserDto(
                1L,
                "vlad@test.ru",
                "Vlad",
                "Ivanov",
                "USER",
                "ACTIVE",
                Instant.now()
        );

        when(authService.me(1L))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("vlad@test.ru"))
                .andExpect(jsonPath("$.firstName").value("Vlad"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"));

        // Verify
        verify(authService, times(1))
                .me(1L);
    }

}
