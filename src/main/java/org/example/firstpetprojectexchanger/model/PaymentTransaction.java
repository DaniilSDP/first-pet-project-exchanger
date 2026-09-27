package org.example.firstpetprojectexchanger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    public enum PaymentType {TOP_UP, WITHDRAW}

    public enum PaymentStatus {PENDING, COMPLETED, FAILED, ROLLED_BACK}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private PaymentType type;

    @Column(nullable = false, precision = 28, scale = 12)
    @NotNull
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @NotBlank
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    @NotBlank
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotBlank
    @Column(length = 64, nullable = false)
    private String externalPaymentId;

    @NotBlank
    @Column(length = 1000, nullable = false)
    private String failureReason;

    @NotNull
    private Integer attempts = 0;

    @Version
    @NotNull
    private Long version;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "payment")
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();

    public PaymentTransaction() {
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public @NotNull BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull BigDecimal amount) {
        this.amount = amount;
    }

    public @NotBlank String getCurrency() {
        return currency;
    }

    public void setCurrency(@NotBlank String currency) {
        this.currency = currency;
    }

    public @NotBlank PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(@NotBlank PaymentStatus status) {
        this.status = status;
    }

    public @NotBlank String getExternalPaymentId() {
        return externalPaymentId;
    }

    public void setExternalPaymentId(@NotBlank String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
    }

    public @NotBlank String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(@NotBlank String failureReason) {
        this.failureReason = failureReason;
    }

    public @NotNull Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(@NotNull Integer attempts) {
        this.attempts = attempts;
    }

    public @NotNull Long getVersion() {
        return version;
    }

    public void setVersion(@NotNull Long version) {
        this.version = version;
    }

    public List<LedgerEntry> getLedgerEntries() {
        return ledgerEntries;
    }

    public void setLedgerEntries(List<LedgerEntry> ledgerEntries) {
        this.ledgerEntries = ledgerEntries;
    }
}
/*
   PaymentTransaction

-id: Long
-idempotencyKey: String «UNIQUE»
-user: User «NOT NULL»
-wallet: Wallet «NOT NULL»
-type: PaymentType
-amount: BigDecimal
-currency: String
-status: PaymentStatus = PENDING
-externalPaymentId: String
-failureReason: String
-attempts: int = 0
-version: long «@Version»
-createdAt: Instant
-updatedAt: Instant

+все геттеры/сеттеры полей

-onCreate(): void «@PrePersist»
-onUpdate(): void «@PreUpdate»
 */