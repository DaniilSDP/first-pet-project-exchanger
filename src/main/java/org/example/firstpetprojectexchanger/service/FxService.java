package org.example.firstpetprojectexchanger.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


import org.example.firstpetprojectexchanger.config.LedgerProperties;
import org.example.firstpetprojectexchanger.exception.BadRequestException;
import org.example.firstpetprojectexchanger.model.ExchangeRate;
import org.example.firstpetprojectexchanger.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;



@Service
public class FxService {

    private static final Logger log = LoggerFactory.getLogger(FxService.class);
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final ExchangeRateRepository exchangeRateRepository;
    private final String pivot;
    private volatile Map<String, BigDecimal> pivotRates = Map.of();

    public FxService(ExchangeRateRepository exchangeRateRepository, LedgerProperties properties) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.pivot = properties.fx().pivotCurrency();
        refresh();
    }

   
    public void refresh() {
        List<ExchangeRate> rates = exchangeRateRepository.findByBaseCurrencyOrderByQuoteCurrency(pivot);
        ConcurrentHashMap<String, BigDecimal> map = new ConcurrentHashMap<>();
        for (ExchangeRate r : rates) {
            map.put(r.getQuoteCurrency(), r.getRate());
        }
        map.put(pivot, ONE);
        pivotRates = map;
        log.info("FX rates refreshed: {} currencies against {}", map.size(), pivot);
    }

   
    public BigDecimal rate(String from, String to) {
        normalize(from);
        normalize(to);
        if (from.equals(to)) {
            return ONE;
        }
        BigDecimal fromToPivot = pivotRates.get(from);
        BigDecimal toFromPivot = pivotRates.get(to);
        if (fromToPivot == null || toFromPivot == null) {
            throw new BadRequestException("No exchange rate configured for " + from + " or " + to);
        }
        return toFromPivot.divide(fromToPivot, 12, RoundingMode.HALF_UP);
    }

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        return amount.multiply(rate(from, to)).setScale(8, RoundingMode.HALF_UP);
    }

    public List<String> supportedCurrencies() {
        return pivotRates.keySet().stream().sorted().collect(Collectors.toList());
    }

    private void normalize(String currency) {
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new BadRequestException("Invalid currency: " + currency);
        }
    }
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