package org.example.firstpetprojectexchanger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
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
    private PaymentStatus status;

    @NotBlank
    @Column(name = "external_payment_id", length = 64, nullable = false)
    private String externalPaymentId;

    @NotBlank
    @Column(name = "failure_reason", length = 1000, nullable = false)
    private String failureReason;

    @NotNull
    private Integer attempts;

    @Version
    @NotNull
    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "payment")
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();

    public PaymentTransaction() {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
/*
    payment_transactions
id : BIGINT «PK»
idempotency_key : VARCHAR(64) «UK»
user_id : BIGINT «FK»
wallet_id : BIGINT «FK»
type : VARCHAR(16)
amount : NUMERIC(28,12)
currency : VARCHAR(3)
status : VARCHAR(16)
    external_payment_id : VARCHAR(64)
    failure_reason : VARCHAR(1000)
attempts : INTEGER
version : BIGINT
created_at : TIMESTAMPTZ
updated_at : TIMESTAMPTZ
 */