package controller;

import org.example.firstpetprojectexchanger.controller.ExchangeRateController;
import org.example.firstpetprojectexchanger.dto.ExchangeRateDto;
import org.example.firstpetprojectexchanger.service.ExchangeRateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRateController.class)
public class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExchangeRateService exchangeRateService;


    @Test
    @DisplayName("Получение курсов указанной валюты")
    void list_Success() throws Exception {

        // Arrange
        ExchangeRateDto rate = new ExchangeRateDto(
                "USD",
                "EUR",
                BigDecimal.valueOf(0.9),
                Instant.now()
        );

        when(exchangeRateService.list("USD"))
                .thenReturn(List.of(rate));


        // Act & Assert
        mockMvc.perform(get("/api/exchange-rates")
                        .param("base", "usd"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].baseCurrency").value("USD"))
                .andExpect(jsonPath("$[0].quoteCurrency").value("EUR"))
                .andExpect(jsonPath("$[0].rate").value(0.9));


        // Verify
        verify(exchangeRateService, times(1)).list("USD");
    }
}
