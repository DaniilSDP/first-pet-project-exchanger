package org.example.firstpetprojectexchanger.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import org.example.firstpetprojectexchanger.config.LedgerProperties;
import org.example.firstpetprojectexchanger.dto.TransferDto;
import org.example.firstpetprojectexchanger.dto.TransferRequest;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.security.JwtAuthenticationFilter;
import org.example.firstpetprojectexchanger.security.RateLimitService;
import org.example.firstpetprojectexchanger.service.IdempotencyService;
import org.example.firstpetprojectexchanger.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(
        controllers = TransferController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(TransferControllerTest.TestConfig.class)
class TransferControllerTest {

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private LedgerProperties properties;

    private TransferRequest sampleRequest() {
        return new TransferRequest(1L, 2L, new BigDecimal("50.00"));
    }

    private TransferDto sampleTransfer() {
        return new TransferDto(
                10L,
                1L,
                2L,
                "USD",
                new BigDecimal("50.00"),
                new BigDecimal("46.00"),
                new BigDecimal("0.92"),
                new BigDecimal("0.50"),
                "COMPLETED",
                null,
                Instant.parse("2026-11-15T10:00:00Z"),
                Instant.parse("2026-11-15T10:05:00Z")
        );
    }

    // ---------- POST /api/transfers ----------

    @Test
    @DisplayName("Успешный перевод")
    void transfer_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        // мок идемпотентности должен выполнить переданную лямбду
        when(idempotencyService.execute(any(), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());

        when(transferService.transfer(any(User.class), any(TransferRequest.class)))
                .thenReturn(sampleTransfer());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/transfers")
                            .header("Idempotency-Key", "key-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.fromWalletId").value(1))
                    .andExpect(jsonPath("$.toWalletId").value(2))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.amount").value(50.00))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            // Verify
            verify(transferService).transfer(any(User.class), any(TransferRequest.class));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Перевод без заголовка Idempotency-Key тоже работает")
    void transfer_WithoutIdempotencyKey_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(idempotencyService.execute(any(), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());

        when(transferService.transfer(any(User.class), any(TransferRequest.class)))
                .thenReturn(sampleTransfer());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert (заголовок не передаём)
            mockMvc.perform(post("/api/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest())))
                    .andExpect(status().isOk());

            // Verify
            verify(transferService).transfer(any(User.class), any(TransferRequest.class));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Перевод с отрицательной суммой возвращает 400")
    void transfer_NegativeAmount_BadRequest() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("-50"));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify: до сервиса дойти не должно
            verify(transferService, never()).transfer(any(), any());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Перевод с отсутствующей суммой возвращает 400")
    void transfer_NullAmount_BadRequest() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        TransferRequest request = new TransferRequest(1L, 2L, null);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify
            verify(transferService, never()).transfer(any(), any());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Перевод с суммой больше двух знаков после запятой возвращает 400")
    void transfer_TooManyFractionDigits_BadRequest() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10.123"));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify
            verify(transferService, never()).transfer(any(), any());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------- GET /api/transfers ----------

    @Test
    @DisplayName("Получение списка моих переводов")
    void list_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(transferService.list(1L)).thenReturn(List.of(sampleTransfer()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/transfers"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].status").value("COMPLETED"));

            // Verify
            verify(transferService).list(1L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------- GET /api/transfers/{transferId} ----------

    @Test
    @DisplayName("Получение одного перевода по id")
    void get_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(transferService.get(1L, 10L)).thenReturn(sampleTransfer());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/transfers/{transferId}", 10L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            // Verify
            verify(transferService).get(1L, 10L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}