package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.PaymentStatus;
import org.example.firstpetprojectexchanger.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDto(

        Long id,

        String idempotencyKey,

        Long userId,

        Long walletId,

        PaymentType type,

        BigDecimal amount,

        String currency,

        PaymentStatus status,

        String externalPaymentId,

        String failureReason,

        Integer attempts,

        Long version,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
