package org.example.firstpetprojectexchanger.controller;

import org.example.firstpetprojectexchanger.dto.ExchangeRateDto;
import org.example.firstpetprojectexchanger.dto.ExchangeRateUpdateRequest;
import org.example.firstpetprojectexchanger.dto.PaymentDto;
import org.example.firstpetprojectexchanger.dto.TransferDto;
import org.example.firstpetprojectexchanger.security.JwtAuthenticationFilter;
import org.example.firstpetprojectexchanger.service.ExchangeRateService;
import org.example.firstpetprojectexchanger.service.PaymentSagaService;
import org.example.firstpetprojectexchanger.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@WithMockUser(roles = "ADMIN")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private PaymentSagaService paymentSagaService;


    @Test
    @DisplayName("Успешное создание/обновление курса валют")
    void upsertRate_Success() throws Exception {

        // Arrange
        ExchangeRateUpdateRequest request =
                new ExchangeRateUpdateRequest(
                        new BigDecimal("0.90"),
                        "USD",
                        "EUR"
                );

        ExchangeRateDto response =
                new ExchangeRateDto(
                        "USD",
                        "EUR",
                        new BigDecimal("0.90"),
                        Instant.now()
                );

        when(exchangeRateService.upsert(any(ExchangeRateUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/admin/exchange-rates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.quoteCurrency").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.90));

        // Verify
        verify(exchangeRateService, times(1)).upsert(any(ExchangeRateUpdateRequest.class));

    }



    @Test
    @DisplayName("Успешное удаление курса валют")
    void deleteRate_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/exchange-rates/{base}/{quote}", "USD", "EUR")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Проверяем, что path-переменные дошли до сервиса именно в таком порядке
        verify(exchangeRateService, times(1)).delete("USD", "EUR");
    }



    @Test
    @DisplayName("Получение всех переводов")
    void allTransfers_Success() throws Exception {
        // Arrange
        TransferDto t1 = new TransferDto(
                1L,
                10L,
                20L,
                "USD",
                new BigDecimal("100.00"),
                new BigDecimal("90.00"),
                new BigDecimal("0.90"),
                new BigDecimal("1.00"),
                "COMPLETED",
                null,
                Instant.parse("2026-11-13T10:00:00Z"),
                Instant.parse("2026-11-13T10:01:00Z")
        );

        TransferDto t2 = new TransferDto(
                2L,
                15L,
                25L,
                "EUR",
                new BigDecimal("200.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1.00"),
                new BigDecimal("2.00"),
                "FAILED",
                "Insufficient funds",
                Instant.parse("2026-11-13T11:00:00Z"),
                null
        );
        when(transferService.listAll()).thenReturn(List.of(t1, t2));

        // Act & Assert
        mockMvc.perform(get("/api/admin/transfers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(transferService, times(1)).listAll();
    }


    @Test
    @DisplayName("Получение всех платежей")
    void allPayments_Success() throws Exception {
        PaymentDto p1 = new PaymentDto(
                1L,
                10L,
                "DEPOSIT",
                new BigDecimal("100.00"),
                "USD",
                "COMPLETED",
                "PAY-123456",
                null,
                Instant.parse("2026-11-13T10:00:00Z"),
                Instant.parse("2026-11-13T10:05:00Z")
        );
        when(paymentSagaService.listAll()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(paymentSagaService).listAll();
    }
}