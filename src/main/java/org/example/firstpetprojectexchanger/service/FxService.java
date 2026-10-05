package org.example.firstpetprojectexchanger.service;

public class FxService {
}
/*
FxService

-exchangeRateRepository: ExchangeRateRepository
-pivot: String
-pivotRates: Map<String,BigDecimal> «volatile»
-ONE: BigDecimal = 1

+refresh(): void
+rate(String, String): BigDecimal
+convert(BigDecimal, String, String): BigDecimal
+supportedCurrencies(): List<String>

-normalize(String): void
 */