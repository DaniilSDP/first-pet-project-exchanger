package org.example.firstpetprojectexchanger.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wallets")
public class Wallet {

    public enum WalletStatus {ACTIVE, FROZEN, CLOSED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 28, scale = 12)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
////////////////////???????????????????????????
    @OneToMany(mappedBy = "wallet")
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();

    @OneToMany(mappedBy = "wallet")
    private List<PaymentTransaction> paymentTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "fromWallet")
    private List<Transfer> outgoingTransfers = new ArrayList<>();

    @OneToMany(mappedBy = "toWallet")
    private List<Transfer> incomingTransfers = new ArrayList<>();

    public Wallet() {
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

    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public void setStatus(WalletStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    
    public List<LedgerEntry> getLedgerEntries() {
        return ledgerEntries;
    }

    public void setLedgerEntries(List<LedgerEntry> ledgerEntries) {
        this.ledgerEntries = ledgerEntries;
    }

    public List<PaymentTransaction> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(List<PaymentTransaction> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public List<Transfer> getOutgoingTransfers() {
        return outgoingTransfers;
    }

    public void setOutgoingTransfers(List<Transfer> outgoingTransfers) {
        this.outgoingTransfers = outgoingTransfers;
    }

    public List<Transfer> getIncomingTransfers() {
        return incomingTransfers;
    }

    public void setIncomingTransfers(List<Transfer> incomingTransfers) {
        this.incomingTransfers = incomingTransfers;
    }
}

/*
Wallet

-id: Long
-user: User «LAZY, NOT NULL»
-currency: String «length=3, immutable»
-status: WalletStatus = ACTIVE
-version: long «@Version»
-createdAt: Instant
-balance: BigDecimal = 0 «NUMERIC(28,12)»

+getId()/setId(Long)
+getUser()/setUser(User)
+getCurrency()/setCurrency(String)
+getStatus()/setStatus(Status)
+getVersion(): long
+getBalance()/setBalance(BigDecimal)
+getCreatedAt(): Instant

-onCreate(): void «@PrePersist»
 */