package org.example.firstpetprojectexchanger.service;

import java.util.List;

import org.example.firstpetprojectexchanger.dto.ExchangeRateDto;
import org.example.firstpetprojectexchanger.dto.ExchangeRateUpdateRequest;
import org.example.firstpetprojectexchanger.exception.ConflictException;
import org.example.firstpetprojectexchanger.exception.ResourceNotFoundException;
import org.example.firstpetprojectexchanger.model.ExchangeRate;
import org.example.firstpetprojectexchanger.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final FxService fxService;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, FxService fxService) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.fxService = fxService;
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateDto> list(String baseCurrency) {
        return exchangeRateRepository.findByBaseCurrencyOrderByQuoteCurrency(baseCurrency).stream()
                .map(ExchangeRateDto::from)
                .toList();
    }

    @Transactional
    public ExchangeRateDto upsert(ExchangeRateUpdateRequest request) {
        String base = request.baseCurrency().toUpperCase();
        String quote = request.quoteCurrency().toUpperCase();
        if (base.equals(quote)) {
            throw new ConflictException("Base and quote currencies must differ");
        }
        ExchangeRate rate = exchangeRateRepository.findByBaseCurrencyAndQuoteCurrency(base, quote)
                .orElseGet(() -> {
                    ExchangeRate r = new ExchangeRate();
                    r.setBaseCurrency(base);
                    r.setQuoteCurrency(quote);
                    return r;
                });
        rate.setRate(request.rate());
        exchangeRateRepository.save(rate);
        fxService.refresh();
        return ExchangeRateDto.from(rate);
    }

    @Transactional
    public void delete(String baseCurrency, String quoteCurrency) {
        ExchangeRate rate = exchangeRateRepository
                .findByBaseCurrencyAndQuoteCurrency(baseCurrency.toUpperCase(), quoteCurrency.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Exchange rate not found"));
        exchangeRateRepository.delete(rate);
        fxService.refresh();
    }
}
/*
ExchangeRateService

-exchangeRateRepository: ExchangeRateRepository
-fxService: FxService

+list(String): List<ExchangeRateDto>
+upsert(ExchangeRateUpdateRequest): ExchangeRateDto
+delete(String, String): void
 */