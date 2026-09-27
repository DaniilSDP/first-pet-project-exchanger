package org.example.firstpetprojectexchanger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record TransferRequest(

        Long fromWalletId,

        Long toWalletId,

        @DecimalMin(value = "0.01")
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