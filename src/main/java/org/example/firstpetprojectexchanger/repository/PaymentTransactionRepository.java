package org.example.firstpetprojectexchanger.repository;

import jakarta.persistence.LockModeType;
import org.example.firstpetprojectexchanger.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByIdAndUserId(Long id, Long userId);

    Optional<PaymentTransaction> indByIdempotencyKey(String idempotencyKey);

    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentTransaction> findByIdForUpdate(Long id);

}
/*
PaymentTransactionRepository
+findByIdAndUserId(Long, Long): Optional<PaymentTransaction>
+findByIdempotencyKey(String): Optional<PaymentTransaction>
+findByUserIdOrderByCreatedAtDesc(Long): List<PaymentTransaction>
+findByIdForUpdate(Long): Optional<PaymentTransaction> «PESSIMISTIC_WRITE»
 */