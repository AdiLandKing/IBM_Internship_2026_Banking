package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
import com.elsys.safebanking.exception.ForbiddenAccessException;
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
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final BankingTransactionRepository transactionRepository;
    private final TransactionLogRepository transactionLogRepository;

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
            throw new IllegalStateException("Source account is not active");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination account is not active");
        }

        // 6. Check Balance
        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            return createFailedTransaction(sourceAccount, destinationAccount, request.amount(), request.reason(), "Insufficient funds");
        }

        // 7. Debit and Credit
        sourceAccount.updateBalance(sourceAccount.getBalance().subtract(request.amount()));
        destinationAccount.updateBalance(destinationAccount.getBalance().add(request.amount()));

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        // 8. Persist Success
        BankingTransaction tx = saveTransaction(sourceAccount, destinationAccount, request.amount(), request.reason(), TransactionStatus.COMPLETED);
        saveLog(tx, "Transfer completed successfully");

        return new TransferResponse(tx.getTranId(), TransactionStatus.COMPLETED);
    }

    private TransferResponse createFailedTransaction(BankAccount source, BankAccount dest, BigDecimal amount, String reason, String failureDetail) {
        BankingTransaction tx = saveTransaction(source, dest, amount, reason, TransactionStatus.FAILED);
        saveLog(tx, "Transfer failed: " + failureDetail);
        return new TransferResponse(tx.getTranId(), TransactionStatus.FAILED);
    }

    private BankingTransaction saveTransaction(BankAccount source, BankAccount dest, BigDecimal amount, String reason, TransactionStatus status) {
        BankingTransaction tx = BankingTransaction.builder()
                .sourceAccount(source)
                .destinationAccount(dest)
                .amount(amount)
                .reason(reason)
                .timeStamp(Instant.now())
                .exchangeRateUsed(BigDecimal.ONE)
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