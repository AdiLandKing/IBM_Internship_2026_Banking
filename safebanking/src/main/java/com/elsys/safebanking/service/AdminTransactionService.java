package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.AdminTransactionResponse;
import com.elsys.safebanking.repository.BankingTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTransactionService {

    private final BankingTransactionRepository bankingTransactionRepository;

    public AdminTransactionService(BankingTransactionRepository bankingTransactionRepository) {
        this.bankingTransactionRepository = bankingTransactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminTransactionResponse> getAllTransactions(Pageable pageable) {
        return bankingTransactionRepository.findAll(pageable)
                .map(AdminTransactionResponse::from);
    }
}
