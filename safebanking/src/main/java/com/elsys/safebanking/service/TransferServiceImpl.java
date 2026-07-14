package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
import com.elsys.safebanking.exception.AccountStateConflictException;
import com.elsys.safebanking.exception.ForbiddenAccessException;
import com.elsys.safebanking.exception.InssuficientFundsException;
import com.elsys.safebanking.exception.InvalidRequestException;
import com.elsys.safebanking.exception.ResourceNotFoundException;
import com.elsys.safebanking.model.*;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.BankingTransactionRepository;
import com.elsys.safebanking.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Executes money transfers between bank accounts, applying live FX conversion
 * when the source and destination accounts hold different currencies.
 *
 * <p><strong>Conversion formula:</strong>
 * <pre>
 *   debitedAmount  = request.amount()                        (source-currency)
 *   rate           = ExchangeRateService.getRate(src, dst)   (dst / src)
 *   creditedAmount = debitedAmount × rate                    (destination-currency)
 * </pre>
 * Both amounts are rounded to {@value #BALANCE_SCALE} decimal places using
 * {@link RoundingMode#HALF_EVEN} (banker's rounding) before any balance mutation.
 *
 * <p><strong>FX reliability:</strong> the Frankfurter lookup is guarded by a
 * 5-minute in-memory cache (configurable via {@code app.fx.cache-ttl-seconds}).
 * If the external API is unreachable the call throws
 * {@link com.elsys.safebanking.exception.ExchangeRateUnavailableException}, which
 * propagates out of this transaction and is mapped to {@code 503 Service Unavailable}
 * by {@link com.elsys.safebanking.exception.ApiExceptionHandler}.
 */
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    /** Decimal places used for all balance arithmetic and persisted amounts. */
    static final int BALANCE_SCALE = 2;

    private final BankAccountRepository       bankAccountRepository;
    private final BankingTransactionRepository transactionRepository;
    private final TransactionLogRepository     transactionLogRepository;
    private final ExchangeRateService          exchangeRateService;

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // 1. Strict Input Validation (Guard Block)
        if (request == null || request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0 ||
            request.sourceAccountIban() == null || request.sourceAccountIban().isBlank() ||
            request.destinationAccountIban() == null || request.destinationAccountIban().isBlank() ||
            request.sourceAccountIban().equals(request.destinationAccountIban())) {
            throw new InvalidRequestException("Invalid transfer parameters: amount must be > 0 and IBANs must be valid and distinct.");
        }

        // 2. Resolve Auth Context
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Find Accounts (with proper domain exceptions)
        BankAccount sourceAccount = bankAccountRepository.findByIban(request.sourceAccountIban())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        BankAccount destinationAccount = bankAccountRepository.findByIban(request.destinationAccountIban())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        // 4. Verify Ownership
        if (!sourceAccount.getOwner().getEmail().equals(currentUserEmail)) {
            throw new ForbiddenAccessException("You are not authorized to transfer from this account");
        }

        // 5. Check Status
        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStateConflictException("Source account is not active");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountStateConflictException("Destination account is not active");
        }

        // 6. Fetch FX rate (throws ExchangeRateUnavailableException → 503 if unavailable)
        String srcCurrency = sourceAccount.getCurrency();
        String dstCurrency = destinationAccount.getCurrency();
        BigDecimal fxRate = exchangeRateService.getRate(srcCurrency, dstCurrency);

        BigDecimal debitedAmount  = request.amount().setScale(BALANCE_SCALE, RoundingMode.HALF_EVEN);
        BigDecimal creditedAmount = debitedAmount.multiply(fxRate).setScale(BALANCE_SCALE, RoundingMode.HALF_EVEN);

        // 7. Check Balance
        if (sourceAccount.getBalance().compareTo(debitedAmount) < 0) {
            throw new InssuficientFundsException("Insufficient funds for transfer");
        }

        // 8. Debit source (source currency), credit destination (destination currency)
        sourceAccount.updateBalance(sourceAccount.getBalance().subtract(debitedAmount));
        destinationAccount.updateBalance(destinationAccount.getBalance().add(creditedAmount));

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        // 9. Persist Success
        BankingTransaction tx = saveTransaction(
                sourceAccount, destinationAccount,
                debitedAmount, creditedAmount,
                srcCurrency, dstCurrency, fxRate,
                request.reason(), TransactionStatus.COMPLETED);
        saveLog(tx, "Transfer completed successfully");

        return new TransferResponse(tx.getTranId(), TransactionStatus.COMPLETED);
    }

    private TransferResponse createFailedTransaction(
            BankAccount source, BankAccount dest,
            BigDecimal debitedAmount, BigDecimal creditedAmount,
            String srcCurrency, String dstCurrency, BigDecimal fxRate,
            String reason, String failureDetail) {
        BankingTransaction tx = saveTransaction(
                source, dest,
                debitedAmount, creditedAmount,
                srcCurrency, dstCurrency, fxRate,
                reason, TransactionStatus.FAILED);
        saveLog(tx, "Transfer failed: " + failureDetail);
        return new TransferResponse(tx.getTranId(), TransactionStatus.FAILED);
    }

    private BankingTransaction saveTransaction(
            BankAccount source, BankAccount dest,
            BigDecimal debitedAmount, BigDecimal creditedAmount,
            String srcCurrency, String dstCurrency, BigDecimal fxRate,
            String reason, TransactionStatus status) {
        BankingTransaction tx = BankingTransaction.builder()
        .sourceAccount(source)
        .destinationAccount(dest)
        .amount(debitedAmount)
        .creditedAmount(creditedAmount)
        .sourceCurrency(srcCurrency)
        .destinationCurrency(dstCurrency)
        .reason(reason)
        .timeStamp(Instant.now())
        .exchangeRateUsed(fxRate)
        .status(status)
        .build();
        return transactionRepository.save(tx);
    }

    private void saveLog(BankingTransaction transaction, String message) {
        TransactionLog log = TransactionLog.builder()
                .transaction(transaction)
                .logEntryText(message)
                .timeStamp(Instant.now())
                .build();
        transactionLogRepository.save(log);
    }
}