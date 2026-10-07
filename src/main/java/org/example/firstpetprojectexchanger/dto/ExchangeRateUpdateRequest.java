package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ExchangeRateUpdateRequest(

		@NotBlank 
		@Pattern(regexp = "[A-Z]{3}")
        BigDecimal rate,

        @NotBlank 
        @Pattern(regexp = "[A-Z]{3}")
        String baseCurrency,

        @NotNull 
        @DecimalMin(value = "0.000000000001", message = "Positive only")
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