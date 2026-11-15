package org.example.firstpetprojectexchanger.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.example.firstpetprojectexchanger.dto.CreateWalletRequest;
import org.example.firstpetprojectexchanger.dto.WalletDto;
import org.example.firstpetprojectexchanger.model.LedgerEntry;
import org.example.firstpetprojectexchanger.model.User;
import org.example.firstpetprojectexchanger.model.Wallet;
import org.example.firstpetprojectexchanger.repository.LedgerEntryRepository;
import org.example.firstpetprojectexchanger.security.JwtAuthenticationFilter;
import org.example.firstpetprojectexchanger.service.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(
        controllers = WalletController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(WalletControllerTest.TestConfig.class)
class WalletControllerTest {

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private LedgerEntryRepository ledgerEntryRepository;

    private WalletDto sampleWallet() {
        return new WalletDto(
                10L,                                    // id
                "USD",                                  // currency
                "ACTIVE",                               // status
                new BigDecimal("100.50"),               // balance
                Instant.parse("2026-11-15T10:00:00Z")   // createdAt
        );
    }

    private LedgerEntry sampleLedgerEntry() {
        // LedgerEntryDto.from(...) вызывает entry.getWallet().getId(), поэтому кошелёк обязателен
        Wallet wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "id", 10L);

        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setEntryType(LedgerEntry.LedgerEntryType.CREDIT);
        entry.setAmount(new BigDecimal("50.00"));
        entry.setBalanceAfter(new BigDecimal("150.50"));
        entry.setCurrency("USD");
        entry.setDescription("Top up");
        entry.setOperationKey("op-123");

        // у этих полей нет сеттеров: id генерирует БД, createdAt ставит @PrePersist
        ReflectionTestUtils.setField(entry, "id", 100L);
        ReflectionTestUtils.setField(entry, "createdAt", Instant.parse("2026-11-15T10:00:00Z"));

        return entry;
    }

    // ---------- POST /api/wallets ----------

    @Test
    @DisplayName("Успешное создание кошелька")
    void create_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        CreateWalletRequest request = new CreateWalletRequest("USD");

        when(walletService.create(eq(1L), any(CreateWalletRequest.class)))
                .thenReturn(sampleWallet());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.balance").value(100.50));

            // Verify: id пользователя взят из принципала
            verify(walletService).create(eq(1L), any(CreateWalletRequest.class));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Создание кошелька с валютой в нижнем регистре возвращает 400")
    void create_LowercaseCurrency_BadRequest() throws Exception {

        // Arrange: "usd" не проходит @Pattern("[A-Z]{3}")
        User user = new User();
        user.setId(1L);

        CreateWalletRequest request = new CreateWalletRequest("usd");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify: до сервиса дойти не должно
            verify(walletService, never()).create(any(), any());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Создание кошелька без валюты возвращает 400")
    void create_NullCurrency_BadRequest() throws Exception {

        // Arrange: null не проходит @NotBlank
        User user = new User();
        user.setId(1L);

        CreateWalletRequest request = new CreateWalletRequest(null);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(post("/api/wallets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Verify
            verify(walletService, never()).create(any(), any());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------- GET /api/wallets ----------

    @Test
    @DisplayName("Получение списка моих кошельков")
    void list_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(walletService.list(1L)).thenReturn(List.of(sampleWallet()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/wallets"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(10))
                    .andExpect(jsonPath("$[0].currency").value("USD"));

            // Verify
            verify(walletService).list(1L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------- GET /api/wallets/{walletId} ----------

    @Test
    @DisplayName("Получение одного кошелька по id")
    void get_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(walletService.get(1L, 10L)).thenReturn(sampleWallet());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/wallets/{walletId}", 10L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.balance").value(100.50));

            // Verify
            verify(walletService).get(1L, 10L);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    // ---------- GET /api/wallets/{walletId}/ledger ----------

    @Test
    @DisplayName("Получение истории операций кошелька")
    void ledger_Success() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        PageImpl<LedgerEntry> page =
                new PageImpl<>(List.of(sampleLedgerEntry()), PageRequest.of(0, 20), 1);

        when(ledgerEntryRepository
                .findByWalletIdOrderByCreatedAtDescIdDesc(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act & Assert (структуру ответа смотри в Body из print())
            mockMvc.perform(get("/api/wallets/{walletId}/ledger", 10L))
                    .andDo(print())
                    .andExpect(status().isOk());

            // Verify: сначала проверка владельца, потом запрос в репозиторий
            verify(walletService).requireOwned(1L, 10L);
            verify(ledgerEntryRepository)
                    .findByWalletIdOrderByCreatedAtDescIdDesc(eq(10L), any(Pageable.class));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Ledger: без параметров используются page=0 и size=20")
    void ledger_DefaultPaging() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(ledgerEntryRepository
                .findByWalletIdOrderByCreatedAtDescIdDesc(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<LedgerEntry>(List.of()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act
            mockMvc.perform(get("/api/wallets/{walletId}/ledger", 10L))
                    .andExpect(status().isOk());

            // Assert: достаём Pageable, который контроллер передал в репозиторий
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(ledgerEntryRepository)
                    .findByWalletIdOrderByCreatedAtDescIdDesc(eq(10L), captor.capture());

            assertEquals(0, captor.getValue().getPageNumber());
            assertEquals(20, captor.getValue().getPageSize());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Ledger: размер страницы ограничен 100")
    void ledger_SizeCappedAt100() throws Exception {

        // Arrange
        User user = new User();
        user.setId(1L);

        when(ledgerEntryRepository
                .findByWalletIdOrderByCreatedAtDescIdDesc(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<LedgerEntry>(List.of()));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        try {
            // Act: просим 500 записей, а также вторую страницу
            mockMvc.perform(get("/api/wallets/{walletId}/ledger", 10L)
                            .param("page", "2")
                            .param("size", "500"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(ledgerEntryRepository)
                    .findByWalletIdOrderByCreatedAtDescIdDesc(eq(10L), captor.capture());

            assertEquals(2, captor.getValue().getPageNumber());
            assertEquals(100, captor.getValue().getPageSize());

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}