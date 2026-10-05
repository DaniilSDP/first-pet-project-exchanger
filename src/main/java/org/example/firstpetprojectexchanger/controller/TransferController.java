package org.example.firstpetprojectexchanger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
public class TransferController {
}
/*
TransferController

-transferService: TransferService
-idempotencyService: IdempotencyService
-rateLimitService: RateLimitService
-properties: LedgerProperties

+transfer(User, TransferRequest, String): ResponseEntity<?>
+list(User): ResponseEntity<List<TransferDto>>
+get(User, Long): ResponseEntity<TransferDto>
 */