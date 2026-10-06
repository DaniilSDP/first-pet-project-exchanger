package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
		
		@NotNull
        Long fromWalletId,
        
        @NotNull
        Long toWalletId,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount at least 0.01")
        @DecimalMax(value = "1000000", message = "Amount too large")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount
) {
}
/*
«record»
TransferRequest
+fromWalletId: Long
+toWalletId: Long
+amount: BigDecimal «@DecimalMin(0.01) @Digits(10,2)»
 */