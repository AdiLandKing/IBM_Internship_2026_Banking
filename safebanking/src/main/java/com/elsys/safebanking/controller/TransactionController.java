package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.*;
import com.elsys.safebanking.model.BankingTransaction;
import com.elsys.safebanking.repository.BankingTransactionRepository;
import com.elsys.safebanking.repository.UserRepository;
import com.elsys.safebanking.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransferService transferService;
    private final BankingTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionHistoryResponse>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        Page<BankingTransaction> transactions;
        if (from != null && to != null) {
            transactions = transactionRepository.findUserTransactionsInDateRange(userId, from, to, pageable);
        } else {
            transactions = transactionRepository.findAllUserTransactions(userId, pageable);
        }

        return ResponseEntity.ok(transactions.map(t -> new TransactionHistoryResponse(
                t.getTranId(),
                t.getSourceAccount().getIban(),
                t.getDestinationAccount().getIban(),
                t.getAmount(),
                t.getReason(),
                t.getStatus(),
                t.getTimeStamp()
        )));
    }
}