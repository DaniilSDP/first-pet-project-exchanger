package org.example.firstpetprojectexchanger.repository;

import jakarta.persistence.LockModeType;
import org.example.firstpetprojectexchanger.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> lockNextBatch(Instant time, int limit);

    Long countByStatus (OutboxEvent.OutboxStatus status);

    Optional<OutboxEvent> findFirstByEventTypeOrderByIdDesc(String eventType);

}

/*
OutboxRepository
+lockNextBatch(Instant, int): List<OutboxEvent> «FOR UPDATE SKIP LOCKED»
+countByStatus(OutboxEvent.Status): long
+findFirstByEventTypeOrderByIdDesc(String): Optional<OutboxEvent>
 */