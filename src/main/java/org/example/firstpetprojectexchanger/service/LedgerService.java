package org.example.firstpetprojectexchanger.service;

import java.math.BigDecimal;

import org.example.firstpetprojectexchanger.exception.ConflictException;
import org.example.firstpetprojectexchanger.model.LedgerEntry;
import org.example.firstpetprojectexchanger.model.Wallet;
import org.example.firstpetprojectexchanger.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal currentBalance(Wallet wallet) {
        return ledgerEntryRepository.findFirstByWalletIdOrderByIdDesc(wallet.getId())
                .map(LedgerEntry::getBalanceAfter)
                .orElse(BigDecimal.ZERO);
    }

   
    @Transactional
    public BigDecimal post(Wallet wallet, LedgerEntry.LedgerEntryType type, BigDecimal amount, String operationKey,
                           String description) {
        if (ledgerEntryRepository.existsByWalletIdAndOperationKey(wallet.getId(), operationKey)) {
            throw new ConflictException("Ledger entry already exists for wallet " + wallet.getId()
                    + " and operation " + operationKey);
        }
        BigDecimal current = wallet.getBalance();
        BigDecimal balanceAfter = (type == LedgerEntry.LedgerEntryType.CREDIT)
                ? current.add(amount)
                : current.subtract(amount);

        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setEntryType(type);
        entry.setAmount(amount);
        entry.setBalanceAfter(balanceAfter);
        entry.setCurrency(wallet.getCurrency());
        entry.setDescription(description);
        entry.setOperationKey(operationKey);
        ledgerEntryRepository.save(entry);

        wallet.setBalance(balanceAfter);
        return balanceAfter;
    }
}
/*
LedgerService

-ledgerEntryRepository: LedgerEntryRepository

+currentBalance(Wallet): BigDecimal
+post(Wallet, EntryType, BigDecimal, String, String): BigDecimal
 */
