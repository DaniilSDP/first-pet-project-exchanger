package org.example.firstpetprojectexchanger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {
}
/*
-sagaService: PaymentSagaService
-idempotencyService: IdempotencyService
-rateLimitService: RateLimitService
-properties: LedgerProperties

+topUp(User, Long, TopUpRequest, String): ResponseEntity<?>
+withdraw(User, Long, WithdrawRequest, String): ResponseEntity<?>
+list(User): ResponseEntity<List<PaymentDto>>
+get(User, Long): ResponseEntity<PaymentDto>
 */