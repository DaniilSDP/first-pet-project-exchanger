package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ExchangeRateUpdateRequest(

        @DecimalMin(value = "0,01")
        BigDecimal rate,

        @Pattern(regexp = "[A-Z]{3}")
        String baseCurrency,

        @Pattern(regexp = "[A-Z]{3}")
        String quoteCurrency

) {
}
/*
«record»
ExchangeRateUpdateRequest
+rate: BigDecimal «positive»
+baseCurrency: String «@Pattern([A-Z]{3})»
+quoteCurrency: String «@Pattern([A-Z]{3})»
 */