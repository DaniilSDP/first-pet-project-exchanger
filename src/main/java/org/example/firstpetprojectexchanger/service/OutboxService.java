package org.example.firstpetprojectexchanger.service;

public class OutboxService {
}
/*
OutboxService

-outboxRepository: OutboxRepository
-objectMapper: ObjectMapper

+emit(String, String, String, Map<String,Object>): void

-writeJson(Map<String,Object>): String
 */