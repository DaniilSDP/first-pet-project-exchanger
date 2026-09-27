package org.example.firstpetprojectexchanger.repository;

import jakarta.persistence.LockModeType;
import org.example.firstpetprojectexchanger.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserIdOrderByIdAsc(Long id);

    Optional<Wallet> findByIdAndUserId(Long id, Long UserId);

    Boolean existsByUserIdAndCurrency(Long userId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findSystemWallet(String currency, String type);

}

/*
WalletRepository
+findByUserIdOrderByIdAsc(Long): List<Wallet>
+findByIdAndUserId(Long, Long): Optional<Wallet>
+existsByUserIdAndCurrency(Long, String): boolean
+findByIdForUpdate(Long): Optional<Wallet> «PESSIMISTIC_WRITE»
+findSystemWallet(String, String): Optional<Wallet> «PESSIMISTIC_WRITE»
 */