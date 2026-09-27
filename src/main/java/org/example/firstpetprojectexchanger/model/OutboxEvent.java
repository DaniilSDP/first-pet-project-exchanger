package org.example.firstpetprojectexchanger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    public enum OutboxStatus {PENDING, PUBLISHED, FAILED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String aggregateType;

    @Column(nullable = false, length = 64)
    private String aggregateId;

    @Column(nullable = false, length = 64)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    @NotBlank
    private OutboxStatus status = OutboxStatus.PENDING;

    @NotNull
    private Integer attempts = 0;

    @Column(nullable = false)
    private Instant availableAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    @Version
    @NotNull
    private Long version;

    public OutboxEvent() {
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public @NotBlank OutboxStatus getStatus() {
        return status;
    }

    public void setStatus(@NotBlank OutboxStatus status) {
        this.status = status;
    }

    public @NotNull Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(@NotNull Integer attempts) {
        this.attempts = attempts;
    }

    public Instant getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(Instant availableAt) {
        this.availableAt = availableAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }


    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public @NotNull Long getVersion() {
        return version;
    }

    public void setVersion(@NotNull Long version) {
        this.version = version;
    }
}
/*
OutboxEvent

-id: Long
-aggregateType: String
-aggregateId: String
-eventType: String
-status: OutboxStatus = PENDING
-attempts: int = 0
-availableAt: Instant
-createdAt: Instant
-publishedAt: Instant
-version: long «@Version»

+все геттеры/сеттеры полей

-payload: String «JSONB, @JdbcTypeCode(JSON)»

-onCreate(): void «@PrePersist»
 */