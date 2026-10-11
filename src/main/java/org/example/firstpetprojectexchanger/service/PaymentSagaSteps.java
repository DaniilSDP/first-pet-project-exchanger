package org.example.firstpetprojectexchanger.service;

import java.util.HashMap;
import java.util.Map;

import org.example.firstpetprojectexchanger.dto.PaymentDto;
import org.example.firstpetprojectexchanger.exception.BadRequestException;
import org.example.firstpetprojectexchanger.exception.InsufficientFundsException;
import org.example.firstpetprojectexchanger.exception.ResourceNotFoundException;
import org.example.firstpetprojectexchanger.model.LedgerEntry;
import org.example.firstpetprojectexchanger.model.PaymentTransaction;
import org.example.firstpetprojectexchanger.model.Wallet;
import org.example.firstpetprojectexchanger.repository.PaymentTransactionRepository;
import org.example.firstpetprojectexchanger.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PaymentSagaSteps {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaSteps.class);

    private final PaymentTransactionRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final LedgerService ledgerService;
    private final OutboxService outboxService;

    public PaymentSagaSteps(PaymentTransactionRepository paymentRepository,
                            WalletRepository walletRepository,
                            LedgerService ledgerService,
                            OutboxService outboxService) {
        this.paymentRepository = paymentRepository;
        this.walletRepository = walletRepository;
        this.ledgerService = ledgerService;
        this.outboxService = outboxService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentTransaction initiate(Wallet wallet, PaymentTransaction payment) {
        Wallet locked = walletRepository.findByIdForUpdate(wallet.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + wallet.getId()));
        if (locked.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new BadRequestException("Wallet " + wallet.getId() + " is not active");
        }
        payment.setWallet(locked);
        paymentRepository.save(payment);

        if (payment.getType() == PaymentTransaction.PaymentType.WITHDRAW) {
            if (locked.getBalance().compareTo(payment.getAmount()) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds: required " + payment.getAmount() + " " + locked.getCurrency()
                                + ", available " + locked.getBalance() + " " + locked.getCurrency());
            }
            ledgerService.post(locked, LedgerEntry.LedgerEntryType.DEBIT, payment.getAmount(),
                    "payment:" + payment.getId(),
                    "Withdrawal reservation to external bank account");
        }
        return payment;
    }

    @Transactional
    public PaymentTransaction settle(Long paymentId, GatewayResponse gateway) {
        PaymentTransaction payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        if (payment.getStatus() != PaymentTransaction.PaymentStatus.PENDING) {
            return payment;
        }
        payment.setAttempts(payment.getAttempts() + 1);
        payment.setExternalPaymentId(gateway.id());

        if (payment.getType() == PaymentTransaction.PaymentType.TOP_UP) {
            ledgerService.post(payment.getWallet(), LedgerEntry.LedgerEntryType.CREDIT, payment.getAmount(),
                    "payment:" + payment.getId(), "Top-up via external gateway " + gateway.id());
        }
        payment.setStatus(PaymentTransaction.PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        outboxService.emit("payment", String.valueOf(payment.getId()), "PAYMENT_COMPLETED", eventPayload(payment));
        log.info("Payment {} settled: {} {} {}", payment.getId(), payment.getAmount(), payment.getCurrency(),
                payment.getStatus());
        return payment;
    }

    @Transactional
    public PaymentTransaction compensate(Long paymentId, Throwable failure) {
        PaymentTransaction payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        if (payment.getStatus() != PaymentTransaction.PaymentStatus.PENDING) {
            return payment;
        }
        payment.setAttempts(payment.getAttempts() + 1);
        payment.setFailureReason(failure.getMessage());

        if (payment.getType() == PaymentTransaction.PaymentType.WITHDRAW) {
            ledgerService.post(payment.getWallet(), LedgerEntry.LedgerEntryType.CREDIT, payment.getAmount(),
                    "payment:" + payment.getId() + ":refund",
                    "Compensating refund of failed withdrawal");
        }
        payment.setStatus(PaymentTransaction.PaymentStatus.ROLLED_BACK);
        paymentRepository.save(payment);

        outboxService.emit("payment", String.valueOf(payment.getId()), "PAYMENT_FAILED", eventPayload(payment));
        log.warn("Payment {} rolled back: {}", payment.getId(), failure.getMessage());
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentDto getPayment(Long userId, Long paymentId) {
        return paymentRepository.findByIdAndUserId(paymentId, userId)
                .map(PaymentDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }

    @Transactional(readOnly = true)
    public java.util.List<PaymentDto> listPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<PaymentDto> listAllPayments() {
        return paymentRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 500))
                .map(PaymentDto::from).stream().toList();
    }

    private Map<String, Object> eventPayload(PaymentTransaction payment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", payment.getId());
        payload.put("userId", payment.getUser().getId());
        payload.put("walletId", payment.getWallet().getId());
        payload.put("type", payment.getType().name());
        payload.put("amount", payment.getAmount());
        payload.put("currency", payment.getCurrency());
        payload.put("status", payment.getStatus().name());
        payload.put("externalPaymentId", payment.getExternalPaymentId());
        return payload;
    }
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