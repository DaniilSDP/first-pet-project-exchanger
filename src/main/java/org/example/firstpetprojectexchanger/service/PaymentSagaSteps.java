package org.example.firstpetprojectexchanger.service;

public class PaymentSagaSteps {
}
/*
PaymentSagaSteps

-paymentRepository: PaymentTransactionRepository
-walletRepository: WalletRepository
-ledgerService: LedgerService
-outboxService: OutboxService

+initiate(Wallet, PaymentTransaction): PaymentTransaction
+settle(Long, GatewayResponse): PaymentTransaction
+compensate(Long, Throwable): PaymentTransaction
+getPayment(Long, Long): PaymentDto
+listPayments(Long): List<PaymentDto>
+listAllPayments(): List<PaymentDto>

-eventPayload(PaymentTransaction): Map<String,Object>
 */