package org.example.firstpetprojectexchanger.service;

public class TransferService {
}
/*
TransferService

-transferRepository: TransferRepository
-walletRepository: WalletRepository
-ledgerService: LedgerService
-fxService: FxService
-outboxService: OutboxService
-properties: LedgerProperties
-MONEY_SCALE: int = 8

+transfer(User, TransferRequest): TransferDto
+get(Long, Long): TransferDto
+list(Long): List<TransferDto>
+listAll(): List<TransferDto>

-computeFee(BigDecimal): BigDecimal
-requireActive(Wallet): void
-eventPayload(Transfer): Map<String,Object>
 */