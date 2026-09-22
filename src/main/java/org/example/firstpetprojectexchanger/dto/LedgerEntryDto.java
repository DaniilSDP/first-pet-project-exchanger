package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.LedgerEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerEntryDto(

        Long id,

        Long walletId,

        Long transferId,

        Long paymentId,

        LedgerEntryType entryType,

        BigDecimal amount,

        BigDecimal balanceAfter,

        String currency,

        String description,

        String operationKey,

        LocalDateTime createdAt

) {
}
