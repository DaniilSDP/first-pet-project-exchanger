package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.ExchangeRate;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateDto(

        String baseCurrency,
        String quoteCurrency,
        BigDecimal rate,
        Instant updatedAt

) {

    public static ExchangeRateDto from(ExchangeRate exchangeRate) {

        return new ExchangeRateDto(

                exchangeRate.getBaseCurrency(),
                exchangeRate.getQuoteCurrency(),
                exchangeRate.getRate(),
                exchangeRate.getUpdatedAt()

        );
    }
}

/*
ExchangeRateDto
+baseCurrency: String
+quoteCurrency: String
+rate: BigDecimal
+updatedAt: Instant

+from(ExchangeRate): ExchangeRateDto
 */
