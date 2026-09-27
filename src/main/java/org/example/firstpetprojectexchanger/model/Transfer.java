package org.example.firstpetprojectexchanger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfers")
public class Transfer {

    public enum TransferStatus {PENDING, COMPLETED, ROLLED_BACK, FAILED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 28, scale = 12)
    private BigDecimal amount;

    @Column(precision = 28, scale = 12)
    private BigDecimal convertedAmount;

    @Column(precision = 28, scale = 12)
    private BigDecimal fxRate;

    @Column(nullable = false, precision = 28, scale = 12)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransferStatus status = TransferStatus.PENDING;

    @Column(length = 1000)
    private String errorMessage;

    @Version
    @NotNull
    private Long version;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    @OneToMany(mappedBy = "transfer")
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();

    public Transfer() {
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

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Wallet getFromWallet() {
        return fromWallet;
    }

    public void setFromWallet(Wallet fromWallet) {
        this.fromWallet = fromWallet;
    }

    public Wallet getToWallet() {
        return toWallet;
    }

    public void setToWallet(Wallet toWallet) {
        this.toWallet = toWallet;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public BigDecimal getFxRate() {
        return fxRate;
    }

    public void setFxRate(BigDecimal fxRate) {
        this.fxRate = fxRate;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public @NotNull Long getVersion() {
        return version;
    }

    public void setVersion(@NotNull Long version) {
        this.version = version;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<LedgerEntry> getLedgerEntries() {
        return ledgerEntries;
    }

    public void setLedgerEntries(List<LedgerEntry> ledgerEntries) {
        this.ledgerEntries = ledgerEntries;
    }
}
/*
 Transfer

-id: Long
-idempotencyKey: String «UNIQUE»
-user: User «NOT NULL»
-fromWallet: Wallet «LAZY»
-toWallet: Wallet «LAZY»
-currency: String
-amount: BigDecimal
-convertedAmount: BigDecimal
-fxRate: BigDecimal
-fee: BigDecimal = 0
-status: TransferStatus = PENDING
-errorMessage: String
-version: long «@Version»
-createdAt: Instant
-completedAt: Instant

+все геттеры/сеттеры полей

+getIdempotencyKey()/setIdempotencyKey(String)

+setCompletedAt(Instant)

-onCreate(): void «@PrePersist»
 */