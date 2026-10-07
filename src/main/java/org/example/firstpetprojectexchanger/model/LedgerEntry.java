package org.example.firstpetprojectexchanger.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "ledger_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ledger_wallet_operation",
                        columnNames = {"wallet_id", "operation_key"}
                )
        }
)
public class LedgerEntry {

    public enum LedgerEntryType {DEBIT, CREDIT}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentTransaction payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 8)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 28, scale = 12)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 28, scale = 12)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String description;

    @Column(name = "operation_key", nullable = false, length = 120)
    private String operationKey;

    @Column(nullable = false)
    private Instant createdAt;

    public LedgerEntry() {
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }


    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public PaymentTransaction getPayment() {
        return payment;
    }

    public void setPayment(PaymentTransaction payment) {
        this.payment = payment;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(LedgerEntryType entryType) {
        this.entryType = entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperationKey() {
        return operationKey;
    }

    public void setOperationKey(String operationKey) {
        this.operationKey = operationKey;
    }
}
/*
   LedgerEntry

-id: Long
-wallet: Wallet «NOT NULL»
-transfer: Transfer «nullable FK»
-payment: PaymentTransaction «nullable FK»
-entryType: EntryType
-amount: BigDecimal
-balanceAfter: BigDecimal
-currency: String
-description: String
-createdAt: Instant

+все геттеры/сеттеры полей

-operationKey: String «UNIQUE(wallet_id, operation_key)»

-onCreate(): void «@PrePersist»
 */