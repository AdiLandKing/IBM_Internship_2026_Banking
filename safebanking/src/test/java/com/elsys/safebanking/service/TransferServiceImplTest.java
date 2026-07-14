package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
import com.elsys.safebanking.exception.AccountStateConflictException;
import com.elsys.safebanking.exception.ExchangeRateUnavailableException;
import com.elsys.safebanking.exception.ForbiddenAccessException;
import com.elsys.safebanking.exception.InssuficientFundsException;
import com.elsys.safebanking.exception.InvalidRequestException;
import com.elsys.safebanking.exception.ResourceNotFoundException;
import com.elsys.safebanking.model.*;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.BankingTransactionRepository;
import com.elsys.safebanking.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock private BankAccountRepository       bankAccountRepository;
    @Mock private BankingTransactionRepository transactionRepository;
    @Mock private TransactionLogRepository     transactionLogRepository;
    @Mock private ExchangeRateService          exchangeRateService;

    @InjectMocks
    private TransferServiceImpl service;

    private User owner;
    private User recipient;

    @BeforeEach
    void setUp() {
        owner     = new User("owner@example.com",     "hash", "Alice", "Smith");
        recipient = new User("recipient@example.com", "hash", "Bob",   "Jones");

        // Simulate an authenticated session for "owner@example.com".
        // lenient() because Mockito cannot statically tell which tests reach the auth check.
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("owner@example.com");
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // -------------------------------------------------------------------------
    // Same-currency transfer
    // -------------------------------------------------------------------------

    @Test
    void transfer_sameCurrency_debitsSourceAndCreditsDestinationWithoutConversion() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "1000.00", owner);
        BankAccount dst = accountOf("IBAN-DST", "EUR", "200.00",  recipient);

        stubAccounts(src, dst);
        when(exchangeRateService.getRate("EUR", "EUR")).thenReturn(BigDecimal.ONE);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("250.00"), "Rent");
        TransferResponse resp = service.transfer(req);

        assertThat(resp.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(src.getBalance()).isEqualByComparingTo("750.00");
        assertThat(dst.getBalance()).isEqualByComparingTo("450.00");

        ArgumentCaptor<BankingTransaction> txCaptor = ArgumentCaptor.forClass(BankingTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        BankingTransaction persisted = txCaptor.getValue();
        assertThat(persisted.getExchangeRateUsed()).isEqualByComparingTo("1");
        assertThat(persisted.getCreditedAmount()).isEqualByComparingTo("250.00");
        assertThat(persisted.getSourceCurrency()).isEqualTo("EUR");
        assertThat(persisted.getDestinationCurrency()).isEqualTo("EUR");
    }

    // -------------------------------------------------------------------------
    // Cross-currency transfer
    // -------------------------------------------------------------------------

    @Test
    void transfer_crossCurrency_appliesFxRateAndCreditsConvertedAmount() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "1000.00", owner);
        BankAccount dst = accountOf("IBAN-DST", "USD", "0.00",    recipient);

        stubAccounts(src, dst);
        when(exchangeRateService.getRate("EUR", "USD")).thenReturn(new BigDecimal("1.0823"));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("100.00"), "Invoice");
        service.transfer(req);

        // source is debited in EUR
        assertThat(src.getBalance()).isEqualByComparingTo("900.00");
        // destination is credited in USD: 100 × 1.0823 = 108.23
        assertThat(dst.getBalance()).isEqualByComparingTo("108.23");

        ArgumentCaptor<BankingTransaction> txCaptor = ArgumentCaptor.forClass(BankingTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        BankingTransaction persisted = txCaptor.getValue();
        assertThat(persisted.getAmount()).isEqualByComparingTo("100.00");
        assertThat(persisted.getCreditedAmount()).isEqualByComparingTo("108.23");
        assertThat(persisted.getExchangeRateUsed()).isEqualByComparingTo("1.0823");
        assertThat(persisted.getSourceCurrency()).isEqualTo("EUR");
        assertThat(persisted.getDestinationCurrency()).isEqualTo("USD");
    }

    // -------------------------------------------------------------------------
    // Rounding
    // -------------------------------------------------------------------------

    @Test
    void transfer_crossCurrency_creditedAmountIsRoundedHalfEvenToTwoDecimalPlaces() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "1000.00", owner);
        BankAccount dst = accountOf("IBAN-DST", "GBP", "0.00",    recipient);

        stubAccounts(src, dst);
        // 100 × 0.8612 = 86.12 exactly — no rounding needed
        // Use a rate that requires rounding: 100 × 0.86125 = 86.125 → rounds to 86.12 (HALF_EVEN)
        when(exchangeRateService.getRate("EUR", "GBP")).thenReturn(new BigDecimal("0.86125"));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("100.00"), "Test");
        service.transfer(req);

        // 100 × 0.86125 = 86.125 → HALF_EVEN rounds to 86.12 (digit before is even)
        assertThat(dst.getBalance()).isEqualByComparingTo("86.12");
    }

    // -------------------------------------------------------------------------
    // FX service failure
    // -------------------------------------------------------------------------

    @Test
    void transfer_fxServiceUnavailable_propagatesExchangeRateUnavailableException() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "1000.00", owner);
        BankAccount dst = accountOf("IBAN-DST", "USD", "0.00",    recipient);

        stubAccounts(src, dst);
        when(exchangeRateService.getRate("EUR", "USD"))
                .thenThrow(new ExchangeRateUnavailableException("EUR", "USD",
                        ExchangeRateUnavailableException.ErrorCode.UPSTREAM_ERROR));

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("50.00"), "Salary");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(ExchangeRateUnavailableException.class);

        // Balances must not have been touched
        assertThat(src.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(dst.getBalance()).isEqualByComparingTo("0.00");
        verify(transactionRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Insufficient funds
    // -------------------------------------------------------------------------

    @Test
    void transfer_insufficientFunds_throwsInssuficientFundsException() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "50.00",  owner);
        BankAccount dst = accountOf("IBAN-DST", "EUR", "100.00", recipient);

        stubAccounts(src, dst);
        when(exchangeRateService.getRate("EUR", "EUR")).thenReturn(BigDecimal.ONE);

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("100.00"), "Too much");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(InssuficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

        assertThat(src.getBalance()).isEqualByComparingTo("50.00");
        assertThat(dst.getBalance()).isEqualByComparingTo("100.00");
        verify(transactionRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Ownership enforcement
    // -------------------------------------------------------------------------

    @Test
    void transfer_sourceAccountBelongsToOtherUser_throwsForbiddenAccessException() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "500.00", recipient); // owned by someone else
        BankAccount dst = accountOf("IBAN-DST", "EUR", "0.00",   recipient);

        stubAccounts(src, dst);

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("100.00"), "Hack");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    // -------------------------------------------------------------------------
    // Account not found
    // -------------------------------------------------------------------------

    @Test
    void transfer_sourceAccountNotFound_throwsResourceNotFoundException() {
        when(bankAccountRepository.findByIban("IBAN-MISSING")).thenReturn(Optional.empty());

        TransferRequest req = new TransferRequest("IBAN-MISSING", "IBAN-DST", new BigDecimal("10.00"), "Test");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void transfer_destinationAccountNotFound_throwsResourceNotFoundException() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "500.00", owner);
        when(bankAccountRepository.findByIban("IBAN-SRC")).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByIban("IBAN-MISSING")).thenReturn(Optional.empty());

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-MISSING", new BigDecimal("10.00"), "Test");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Blocked accounts
    // -------------------------------------------------------------------------

    @Test
    void transfer_sourceAccountBlocked_throwsAccountStateConflictException() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "500.00", owner);
        src.block();
        BankAccount dst = accountOf("IBAN-DST", "EUR", "0.00", recipient);

        stubAccounts(src, dst);

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("10.00"), "Test");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(AccountStateConflictException.class)
                .hasMessageContaining("Source account is not active");
    }

    @Test
    void transfer_destinationAccountBlocked_throwsAccountStateConflictException() {
        BankAccount src = accountOf("IBAN-SRC", "EUR", "500.00", owner);
        BankAccount dst = accountOf("IBAN-DST", "EUR", "0.00",   recipient);
        dst.block();

        stubAccounts(src, dst);

        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", new BigDecimal("10.00"), "Test");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(AccountStateConflictException.class)
                .hasMessageContaining("Destination account is not active");
    }

    // -------------------------------------------------------------------------
    // Invalid request guard
    // -------------------------------------------------------------------------

    @Test
    void transfer_sameSourceAndDestinationIban_throwsInvalidRequestException() {
        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-SRC", new BigDecimal("10.00"), "Self");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void transfer_zeroAmount_throwsInvalidRequestException() {
        TransferRequest req = new TransferRequest("IBAN-SRC", "IBAN-DST", BigDecimal.ZERO, "Zero");

        assertThatThrownBy(() -> service.transfer(req))
                .isInstanceOf(InvalidRequestException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private BankAccount accountOf(String iban, String currency, String balance, User owner) {
        BankAccount account = new BankAccount(iban, "Test Account", currency, owner);
        account.updateBalance(new BigDecimal(balance));
        return account;
    }

    private void stubAccounts(BankAccount src, BankAccount dst) {
        when(bankAccountRepository.findByIban(src.getIban())).thenReturn(Optional.of(src));
        when(bankAccountRepository.findByIban(dst.getIban())).thenReturn(Optional.of(dst));
    }
}
