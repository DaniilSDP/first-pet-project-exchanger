package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopUpRequest(

        @NotNull
        BigDecimal amount,

        String cardToken

) {
}
/*
«record»
TopUpRequest
+amount: BigDecimal «validated»
+cardToken: String
 */