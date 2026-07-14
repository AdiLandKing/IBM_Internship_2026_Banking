package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankingTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {

    @Query("SELECT t FROM BankingTransaction t WHERE t.sourceAccount.owner.id = :userId OR t.destinationAccount.owner.id = :userId")
    Page<BankingTransaction> findAllUserTransactions(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM BankingTransaction t WHERE (t.sourceAccount.owner.id = :userId OR t.destinationAccount.owner.id = :userId) AND t.timeStamp BETWEEN :startDate AND :endDate")
    Page<BankingTransaction> findUserTransactionsInDateRange(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}