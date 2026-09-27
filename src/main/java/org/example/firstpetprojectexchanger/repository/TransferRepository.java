package org.example.firstpetprojectexchanger.repository;

import jakarta.persistence.LockModeType;
import org.example.firstpetprojectexchanger.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByIdAndUserId(Long id, Long userId);

    List<Transfer> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Transfer> findByIdForUpdate(Long id);

}

/*
TransferRepository
+findByIdAndUserId(Long, Long): Optional<Transfer>
+findByUserIdOrderByCreatedAtDesc(Long): List<Transfer>
+findByIdempotencyKey(String): Optional<Transfer>
+findByIdForUpdate(Long): Optional<Transfer> «PESSIMISTIC_WRITE»
 */