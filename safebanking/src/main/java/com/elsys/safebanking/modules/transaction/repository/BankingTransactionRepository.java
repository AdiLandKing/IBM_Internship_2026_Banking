package com.elsys.safebanking.modules.transaction.repository;

import com.elsys.safebanking.modules.transaction.entity.BankingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {
    // Placeholder: add transaction history queries by account, user, and date range.
}
