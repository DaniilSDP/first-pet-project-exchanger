package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferDto(

        Long id,

        String idempotencyKey,

        Long userId,

        Long fromWalletId,

        Long toWalletId,

        String currency,

        BigDecimal amount,

        BigDecimal convertedAmount,

        BigDecimal fxRate,

        BigDecimal fee,

        TransferStatus status,

        String errorMessage,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime completedAt

) {
}
