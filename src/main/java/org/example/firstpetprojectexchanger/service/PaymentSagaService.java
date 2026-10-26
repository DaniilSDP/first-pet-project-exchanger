package org.example.firstpetprojectexchanger.service;

import java.math.BigDecimal;

import org.example.firstpetprojectexchanger.dto.PaymentDto;
import org.example.firstpetprojectexchanger.dto.TopUpRequest;
import org.example.firstpetprojectexchanger.dto.WithdrawRequest;
import org.example.firstpetprojectexchanger.exception.ResourceNotFoundException;
import org.example.firstpetprojectexchanger.gateway.GatewayResponse;
import org.example.firstpetprojectexchanger.gateway.MockGatewayClient;
import org.example.firstpetprojectexchanger.model.PaymentTransaction;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.model.Wallet;
import org.example.firstpetprojectexchanger.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class PaymentSagaService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaService.class);

    private final WalletRepository walletRepository;
    private final MockGatewayClient gatewayClient;
    private final PaymentSagaSteps steps;

    public PaymentSagaService(WalletRepository walletRepository,
                              MockGatewayClient gatewayClient,
                              PaymentSagaSteps steps) {
        this.walletRepository = walletRepository;
        this.gatewayClient = gatewayClient;
        this.steps = steps;
    }

    public PaymentDto topUp(User user, Long walletId, TopUpRequest request, String idempotencyKey) {
        return runTopUp(user, walletId, request.amount(), idempotencyKey);
    }

    public PaymentDto withdraw(User user, Long walletId, WithdrawRequest request, String idempotencyKey) {
        return runWithdraw(user, walletId, request.amount(), idempotencyKey);
    }

    private PaymentDto runTopUp(User user, Long walletId, BigDecimal amount, String idempotencyKey) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Wallet not found with id: " + walletId);
        }

        PaymentTransaction payment = new PaymentTransaction();
        payment.setIdempotencyKey(idempotencyKey);
        payment.setUser(user);
        payment.setWallet(wallet);
        payment.setType(PaymentTransaction.PaymentType.TOP_UP);
        payment.setAmount(amount);
        payment.setCurrency(wallet.getCurrency());
        payment.setStatus(PaymentTransaction.PaymentStatus.PENDING);

        PaymentTransaction initiated = steps.initiate(wallet, payment);
        try {
            GatewayResponse gateway = gatewayClient.charge(initiated.getCurrency(),
                    "topup-" + initiated.getId(), amount);
            return PaymentDto.from(steps.settle(initiated.getId(), gateway));
        } catch (Exception e) {
            return PaymentDto.from(steps.compensate(initiated.getId(), e));
        }
    }

    private PaymentDto runWithdraw(User user, Long walletId, BigDecimal amount, String idempotencyKey) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Wallet not found with id: " + walletId);
        }

        PaymentTransaction payment = new PaymentTransaction();
        payment.setIdempotencyKey(idempotencyKey);
        payment.setUser(user);
        payment.setWallet(wallet);
        payment.setType(PaymentTransaction.PaymentType.WITHDRAW);
        payment.setAmount(amount);
        payment.setCurrency(wallet.getCurrency());
        payment.setStatus(PaymentTransaction.PaymentStatus.PENDING);

        PaymentTransaction initiated = steps.initiate(wallet, payment);
        try {
            GatewayResponse gateway = gatewayClient.payout(initiated.getCurrency(),
                    "withdraw-" + initiated.getId(), amount);
            return PaymentDto.from(steps.settle(initiated.getId(), gateway));
        } catch (Exception e) {
            return PaymentDto.from(steps.compensate(initiated.getId(), e));
        }
    }

    @Transactional(readOnly = true)
    public PaymentDto get(Long userId, Long paymentId) {
        return steps.getPayment(userId, paymentId);
    }

    @Transactional(readOnly = true)
    public java.util.List<PaymentDto> list(Long userId) {
        return steps.listPayments(userId);
    }

    @Transactional(readOnly = true)
    public java.util.List<PaymentDto> listAll() {
        return steps.listAllPayments();
    }
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