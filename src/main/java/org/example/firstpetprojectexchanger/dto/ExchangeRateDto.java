package org.example.firstpetprojectexchanger.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateDto(

        Long id,

        String baseCurrency,

        String quoteCurrency,

        BigDecimal rate,

        LocalDateTime updatedAt

) {
}
