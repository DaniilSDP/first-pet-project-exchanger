package org.example.firstpetprojectexchanger.controller;

import java.util.List;

import org.example.firstpetprojectexchanger.dto.ExchangeRateDto;
import org.example.firstpetprojectexchanger.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/exchange-rates")
//@Tag(name = "Exchange Rates", description = "Public currency rates (EUR pivot)")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    //@Operation(summary = "List rates for a base currency (default EUR)")
    public ResponseEntity<List<ExchangeRateDto>> list(@RequestParam(defaultValue = "EUR") String base) {
        return ResponseEntity.ok(exchangeRateService.list(base.toUpperCase()));
    }
}
/*
ExchangeRateController

-exchangeRateService: ExchangeRateService

+list(String): ResponseEntity<List<ExchangeRateDto>>
 */