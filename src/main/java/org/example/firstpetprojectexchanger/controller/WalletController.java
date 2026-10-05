package org.example.firstpetprojectexchanger.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallets")
public class WalletController {
}
/*
WalletController

-walletService: WalletService
-ledgerEntryRepository: LedgerEntryRepository

+create(User, CreateWalletRequest): ResponseEntity<WalletDto>
+list(User): ResponseEntity<List<WalletDto>>
+get(User, Long): ResponseEntity<WalletDto>
+ledger(User, Long, int, int): ResponseEntity<PageResponse<LedgerEntryDto>>
 */
