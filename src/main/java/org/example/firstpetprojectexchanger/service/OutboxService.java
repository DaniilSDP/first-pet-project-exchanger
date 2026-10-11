package org.example.firstpetprojectexchanger.service;

import java.util.Map;

import org.example.firstpetprojectexchanger.model.OutboxEvent;
import org.example.firstpetprojectexchanger.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;



@Service
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void emit(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(writeJson(payload));
        outboxRepository.save(event);
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
/*
OutboxService

-outboxRepository: OutboxRepository
-objectMapper: ObjectMapper

+emit(String, String, String, Map<String,Object>): void

-writeJson(Map<String,Object>): String
 */