package org.example.firstpetprojectexchanger.repository;

import jakarta.persistence.LockModeType;
import org.example.firstpetprojectexchanger.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

	@Query(value = """
            select * from outbox_events
            where status = 'PENDING' and available_at <= :now
            order by id
            limit :limit
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("now") Instant now, @Param("limit") int limit);

    Long countByStatus (OutboxEvent.OutboxStatus status);

    Optional<OutboxEvent> findFirstByEventTypeOrderByIdDesc(String eventType);

}

/*
OutboxRepository
+lockNextBatch(Instant, int): List<OutboxEvent> «FOR UPDATE SKIP LOCKED»
+countByStatus(OutboxEvent.Status): long
+findFirstByEventTypeOrderByIdDesc(String): Optional<OutboxEvent>
 */