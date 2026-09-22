package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletDto(

        Long id,

        Long userId,

        String currency,

        BigDecimal balance,

        WalletStatus status,

        Long version,

        LocalDateTime createdAt

) {
}
