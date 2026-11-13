package org.example.firstpetprojectexchanger.controller;

import org.example.firstpetprojectexchanger.dto.ExchangeRateDto;
import org.example.firstpetprojectexchanger.security.JwtAuthenticationFilter;
import org.example.firstpetprojectexchanger.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ExchangeRateController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
public class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("Успешное получение списка курсов по базовой валюте")
    void getListRate_Success() throws Exception {

        // Arrange
        String base = "EUR";

        ExchangeRateDto rate = new ExchangeRateDto(
                "EUR",
                "USD",
                new BigDecimal("0.8"),
                Instant.now()
        );

        when(exchangeRateService.list(base)).thenReturn(List.of(rate));

        // Act & Assert
        mockMvc.perform(get("/api/exchange-rates")
                        .param("base", base))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].baseCurrency").value("EUR"))
                .andExpect(jsonPath("$[0].quoteCurrency").value("USD"))
                .andExpect(jsonPath("$[0].rate").value(0.8))
                .andExpect(jsonPath("$[0].updatedAt").exists());

        // Verify
        verify(exchangeRateService).list(base);
    }

    @Test
    @DisplayName("Курсов нет: возвращается пустой массив")
    void getListRate_Empty() throws Exception {
        when(exchangeRateService.list("EUR")).thenReturn(List.of());

        mockMvc.perform(get("/api/exchange-rates").param("base", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
