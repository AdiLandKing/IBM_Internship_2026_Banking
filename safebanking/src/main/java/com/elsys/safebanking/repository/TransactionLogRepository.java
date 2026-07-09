package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    // Placeholder: add log retrieval methods by transaction id.
}
