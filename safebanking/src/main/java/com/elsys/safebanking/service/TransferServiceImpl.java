package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.TransferRequest;
import com.elsys.safebanking.dto.TransferResponse;
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
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        BankAccount sourceAccount = bankAccountRepository.findByIban(request.sourceAccountIban())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        BankAccount destinationAccount = bankAccountRepository.findByIban(request.destinationAccountIban())
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        if (!sourceAccount.getOwner().getEmail().equals(currentUserEmail)) {
            throw new SecurityException("You are not authorized to transfer from this account");
        }

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Source account is not active");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination account is not active");
        }

        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            return createFailedTransaction(sourceAccount, destinationAccount, request.amount(), request.reason(), "Insufficient funds");
        }

        sourceAccount.updateBalance(sourceAccount.getBalance().subtract(request.amount()));
        destinationAccount.updateBalance(destinationAccount.getBalance().add(request.amount()));

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

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