package org.example.firstpetprojectexchanger.controller;

import org.example.firstpetprojectexchanger.config.LedgerProperties;
import org.example.firstpetprojectexchanger.dto.PaymentDto;
import org.example.firstpetprojectexchanger.dto.TopUpRequest;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.security.JwtAuthenticationFilter;
import org.example.firstpetprojectexchanger.security.RateLimitService;
import org.example.firstpetprojectexchanger.service.IdempotencyService;
import org.example.firstpetprojectexchanger.service.PaymentSagaService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(PaymentControllerTest.TestConfig.class)
class PaymentControllerTest {

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
    private PaymentSagaService sagaService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private LedgerProperties properties;

    // Одинаковый PaymentDto для всех тестов, чтобы не дублировать конструктор
    private PaymentDto samplePayment() {
        return new PaymentDto(
                1L,
                10L,
                "DEPOSIT",
                new BigDecimal("250.00"),
                "USD",
                "COMPLETED",
                "PAY-123456",
                null,
                Instant.parse("2026-11-15T10:00:00Z"),
                Instant.parse("2026-11-15T10:05:00Z")
        );
    }

    @Test
    @DisplayName("Успешное пополнение кошелька")
    void topUp_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        TopUpRequest request = new TopUpRequest(new BigDecimal("100.50"), "tok_123456789");

        // мок идемпотентности должен выполнить переданную лямбду
        when(idempotencyService.execute(any(), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());

        when(sagaService.topUp(any(User.class), eq(5L), any(TopUpRequest.class), eq("key-123")))
                .thenReturn(samplePayment());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/payments/wallets/{walletId}/topup", 5L)
                            .header("Idempotency-Key", "key-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify
            verify(sagaService).topUp(any(User.class), eq(5L), any(TopUpRequest.class), eq("key-123"));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Пополнение без заголовка Idempotency-Key тоже работает")
    void topUp_WithoutIdempotencyKey_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        TopUpRequest request = new TopUpRequest(new BigDecimal("100.50"), "tok_123456789");

        when(idempotencyService.execute(any(), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());

        when(sagaService.topUp(any(User.class), eq(5L), any(TopUpRequest.class), eq(null)))
                .thenReturn(samplePayment());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/payments/wallets/{walletId}/topup", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Verify
            verify(sagaService).topUp(any(User.class), eq(5L), any(TopUpRequest.class), eq(null));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Пополнение с невалидным телом возвращает 400")
    void topUp_InvalidBody_BadRequest() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        TopUpRequest request = new TopUpRequest(new BigDecimal("-5"), "tok_123456789");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/payments/wallets/{walletId}/topup", 5L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify: до сервиса дойти не должно
            verify(sagaService, never()).topUp(any(), any(), any(), any());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Получение списка моих платежей")
    void list_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(sagaService.list(1L)).thenReturn(List.of(samplePayment()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/payments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            // Verify
            verify(sagaService).list(1L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Получение одного платежа по id")
    void get_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(sagaService.get(1L, 10L)).thenReturn(samplePayment());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/payments/{paymentId}", 10L))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify
            verify(sagaService).get(1L, 10L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}