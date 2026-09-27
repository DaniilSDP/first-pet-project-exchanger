package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawRequest(

        @NotNull
        BigDecimal amount,

        String bankAccount

) {
}
/*
«record»
WithdrawRequest
+amount: BigDecimal «validated»
+bankAccount: String
 */