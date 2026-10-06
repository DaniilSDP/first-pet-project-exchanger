package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletDto(

        Long id,
        String currency,
        String status,
        BigDecimal balance,
        Instant createdAt

) {

    public static WalletDto of(

            Wallet wallet,
            BigDecimal balance

    ) {
        return new WalletDto(

                wallet.getId(),
                wallet.getCurrency(),
                wallet.getStatus() != null ? wallet.getStatus().name() : null,
                balance,
                wallet.getCreatedAt()

        );
    }
}

/*
WalletDto

+id: Long
+currency: String
+status: String
+balance: BigDecimal
+createdAt: Instant

+of(Wallet, BigDecimal): WalletDto
 */