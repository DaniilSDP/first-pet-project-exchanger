package controller;

import org.example.firstpetprojectexchanger.controller.AdminController;
import org.example.firstpetprojectexchanger.dto.ExchangeRateDto;
import org.example.firstpetprojectexchanger.dto.ExchangeRateUpdateRequest;
import org.example.firstpetprojectexchanger.dto.RegisterRequest;
import org.example.firstpetprojectexchanger.dto.TransferDto;
import org.example.firstpetprojectexchanger.service.ExchangeRateService;
import org.example.firstpetprojectexchanger.service.PaymentSagaService;
import org.example.firstpetprojectexchanger.service.TransferService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {


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
    @DisplayName("Успешное обновление курса валют")
    void upsertRate_Success() throws Exception {

        // Arrange
        ExchangeRateUpdateRequest request =
                new ExchangeRateUpdateRequest(
                        BigDecimal.valueOf(0.9),
                        "USD",
                        "EUR"
                );

        ExchangeRateDto response =
                new ExchangeRateDto(
                        "USD",
                        "EUR",
                        BigDecimal.valueOf(0.9),
                        Instant.now()
                );

        when(exchangeRateService.upsert(any(ExchangeRateUpdateRequest.class)))
                .thenReturn(response);


        // Act & Assert
        mockMvc.perform(put("/api/admin/exchange-rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.quoteCurrency").value("EUR"))
                .andExpect(jsonPath("$.rate").value(0.9));


        // Verify
        verify(exchangeRateService, times(1))
                .upsert(any(ExchangeRateUpdateRequest.class));
    }


    @Test
    @DisplayName("Успешное удаление курса валют")
    void deleteRate_Success() throws Exception {

        // Arrange
        doNothing().when(exchangeRateService).delete("USD", "EUR");

        // Act & Assert
        mockMvc.perform(delete("/api/admin/exchange-rates/USD/EUR"))
                .andExpect(status().isNoContent());

        // Verify
        verify(exchangeRateService, times(1))
                .delete("USD", "EUR");
    }


    @Test
    @DisplayName("список всех оплат")
    void payments_Shou() throws Exception {

        // Arrange
        when(paymentSagaService.listAll())
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // Verify
        verify(paymentSagaService, times(1)).listAll();
    }


}
