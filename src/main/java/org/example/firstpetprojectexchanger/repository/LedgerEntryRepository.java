package org.example.firstpetprojectexchanger.repository;

import org.example.firstpetprojectexchanger.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    Page<LedgerEntry> findByWalletIdOrderByCreatedAtDescIdDesc(
            Long walletId,
            Pageable pageable
    );

    Optional<LedgerEntry> findFirstByWalletIdOrderByIdDesc(Long walletId);

    Boolean existsByWalletIdAndOperationKey(Long walletId, String operationKey);

}
/*
LedgerEntryRepository
+findByWalletIdOrderByCreatedAtDescIdDesc(Long, Pageable): Page<LedgerEntry>
+findFirstByWalletIdOrderByIdDesc(Long): Optional<LedgerEntry>
+existsByWalletIdAndOperationKey(Long, String): boolean
 */