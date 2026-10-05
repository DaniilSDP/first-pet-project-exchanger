package org.example.firstpetprojectexchanger.service;

public class WalletService {
}
/*
WalletService

-walletRepository: WalletRepository
-userRepository: UserRepository
-ledgerService: LedgerService
-fxService: FxService

+create(Long, CreateWalletRequest): WalletDto
+list(Long): List<WalletDto>
+get(Long, Long): WalletDto
+requireOwned(Long, Long): Wallet
+findOwned(Long, Long): Wallet
 */