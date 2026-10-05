package org.example.firstpetprojectexchanger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admins")
public class AdminController {
}
/*
AdminController

-exchangeRateService: ExchangeRateService
-transferService: TransferService
-paymentSagaService: PaymentSagaService

+upsertRate(ExchangeRateUpdateRequest): ResponseEntity<ExchangeRateDto>
+deleteRate(String, String): ResponseEntity<Void>
+allTransfers(): ResponseEntity<List<TransferDto>>
+allPayments(): ResponseEntity<List<PaymentDto>>
 */