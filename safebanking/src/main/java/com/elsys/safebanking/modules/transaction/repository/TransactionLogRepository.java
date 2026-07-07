package com.elsys.safebanking.modules.transaction.repository;

import com.elsys.safebanking.modules.transaction.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    // Placeholder: add log retrieval methods by transaction id.
}
