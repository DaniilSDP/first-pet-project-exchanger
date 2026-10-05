package org.example.firstpetprojectexchanger.service;

public class PaymentSagaService {
}
/*
PaymentSagaService

-walletRepository: WalletRepository
-gatewayClient: MockGatewayClient
-steps: PaymentSagaSteps

+topUp(User, Long, TopUpRequest, String): PaymentDto
+withdraw(User, Long, WithdrawRequest, String): PaymentDto

-runTopUp(User, Long, BigDecimal, String): PaymentDto
-runWithdraw(User, Long, BigDecimal, String): PaymentDto

+get(Long, Long): PaymentDto
+list(Long): List<PaymentDto>
+listAll(): List<PaymentDto>
 */