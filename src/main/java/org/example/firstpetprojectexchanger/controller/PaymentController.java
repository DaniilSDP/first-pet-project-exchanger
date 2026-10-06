package org.example.firstpetprojectexchanger.controller;

import java.time.Duration;
import java.util.List;

import org.example.firstpetprojectexchanger.dto.PaymentDto;
import org.example.firstpetprojectexchanger.dto.TopUpRequest;
import org.example.firstpetprojectexchanger.dto.WithdrawRequest;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.service.IdempotencyService;
import org.example.firstpetprojectexchanger.service.PaymentSagaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
//@Tag(name = "Payments", description = "Saga-orchestrated top-up and withdrawal via the external gateway")
public class PaymentController {

    private final PaymentSagaService sagaService;
    private final IdempotencyService idempotencyService;
    private final RateLimitService rateLimitService;
    private final LedgerProperties properties;

    public PaymentController(PaymentSagaService sagaService,
                             IdempotencyService idempotencyService,
                             RateLimitService rateLimitService,
                             LedgerProperties properties) {
        this.sagaService = sagaService;
        this.idempotencyService = idempotencyService;
        this.rateLimitService = rateLimitService;
        this.properties = properties;
    }

    @PostMapping("/wallets/{walletId}/topup")
    //@Operation(summary = "Top up a wallet via the external acquiring gateway (saga)")
    public ResponseEntity<?> topUp(@AuthenticationPrincipal User user,
                                   @PathVariable Long walletId,
                                   @Valid @RequestBody TopUpRequest request,
                                   @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        rateLimitService.check("payment:" + user.getId(),
                properties.rateLimit().transferPerMinute(), Duration.ofMinutes(1));
        return idempotencyService.execute(user.getId(), idempotencyKey,
                () -> ResponseEntity.ok(sagaService.topUp(user, walletId, request, idempotencyKey)));
    }

    @PostMapping("/wallets/{walletId}/withdraw")
    //@Operation(summary = "Withdraw from a wallet to an external bank account (saga with compensation)")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal User user,
                                      @PathVariable Long walletId,
                                      @Valid @RequestBody WithdrawRequest request,
                                      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        rateLimitService.check("payment:" + user.getId(),
                properties.rateLimit().transferPerMinute(), Duration.ofMinutes(1));
        return idempotencyService.execute(user.getId(), idempotencyKey,
                () -> ResponseEntity.ok(sagaService.withdraw(user, walletId, request, idempotencyKey)));
    }

    @GetMapping
    //@Operation(summary = "My payments")
    public ResponseEntity<List<PaymentDto>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sagaService.list(user.getId()));
    }

    @GetMapping("/{paymentId}")
    //@Operation(summary = "Get one payment")
    public ResponseEntity<PaymentDto> get(@AuthenticationPrincipal User user, @PathVariable Long paymentId) {
        return ResponseEntity.ok(sagaService.get(user.getId(), paymentId));
    }
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