package org.example.firstpetprojectexchanger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ExchangeRates")
public class ExchangeRateController {
}
/*
ExchangeRateController

-exchangeRateService: ExchangeRateService

+list(String): ResponseEntity<List<ExchangeRateDto>>
 */