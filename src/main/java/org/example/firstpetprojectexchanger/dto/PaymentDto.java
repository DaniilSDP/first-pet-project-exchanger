package org.example.firstpetprojectexchanger.dto;

import org.example.firstpetprojectexchanger.model.PaymentTransaction;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDto(

        Long id,
        Long walletId,
        String type,
        BigDecimal amount,
        String currency,
        String status,
        String externalPaymentId,
        String failureReason,
        Instant createdAt,
        Instant updatedAt

) {
    public static PaymentDto from(PaymentTransaction paymentTransaction) {

        return new PaymentDto(

                paymentTransaction.getId(),
                paymentTransaction.getWallet().getId(),
                paymentTransaction.getType().toString(),
                paymentTransaction.getAmount(),
                paymentTransaction.getCurrency(),
                paymentTransaction.getStatus().toString(),
                paymentTransaction.getExternalPaymentId(),
                paymentTransaction.getFailureReason(),
                paymentTransaction.getCreatedAt(),
                paymentTransaction.getUpdatedAt()

        );
    }
}
/*
PaymentDto

+id / walletId: Long
+type: String
+amount: BigDecimal
+currency: String
+status: String
+externalPaymentId: String
+failureReason: String
+createdAt / updatedAt: Instant

+from(PaymentTransaction): PaymentDto
 */