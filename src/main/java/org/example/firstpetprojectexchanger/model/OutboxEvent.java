package org.example.firstpetprojectexchanger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 32)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    @NotBlank
    private OutboxStatus status;

    @NotNull
    private Integer attempts;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Version
    @NotNull
    private Long version;

    public OutboxEvent() {
    }
}
/*
outbox_events
id : BIGINT «PK»
aggregate_type : VARCHAR(32)
aggregate_id : VARCHAR(64)
event_type : VARCHAR(64)
payload : JSONB
status : VARCHAR(16)
attempts : INTEGER
available_at : TIMESTAMPTZ
created_at : TIMESTAMPTZ
    published_at : TIMESTAMPTZ
version : BIGINT
 */