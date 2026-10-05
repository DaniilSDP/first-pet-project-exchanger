package org.example.firstpetprojectexchanger.service;

public class ExchangeRateService {
}
/*
ExchangeRateService

-exchangeRateRepository: ExchangeRateRepository
-fxService: FxService

+list(String): List<ExchangeRateDto>
+upsert(ExchangeRateUpdateRequest): ExchangeRateDto
+delete(String, String): void
 */