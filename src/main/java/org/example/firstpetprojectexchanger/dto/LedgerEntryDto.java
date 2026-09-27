package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryDto(

        Long id,
        Long walletId,
        Long transferId,
        Long paymentId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String currency,
        String description,
        String operationKey,
        Instant createdAt

) {

    public static LedgerEntryDto from(LedgerEntry ledgerEntry) {
        return new LedgerEntryDto(

                ledgerEntry.getId(),
                ledgerEntry.getWallet().getId(),
                ledgerEntry.getTransfer().getId(),
                ledgerEntry.getPayment().getId(),
                ledgerEntry.getAmount(),
                ledgerEntry.getBalanceAfter(),
                ledgerEntry.getCurrency(),
                ledgerEntry.getDescription(),
                ledgerEntry.getOperationKey(),
                ledgerEntry.getCreatedAt()

        );
    }
}

/*
«record»
LedgerEntryDto

+id / walletId / transferId / paymentId: Long
+entryType: String
+amount / balanceAfter: BigDecimal
+currency: String
+description: String
+operationKey: String
+createdAt: Instant

+from(LedgerEntry): LedgerEntryDto
 */