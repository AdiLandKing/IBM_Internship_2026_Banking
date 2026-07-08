package com.elsys.safebanking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tran_id", nullable = false)
    private BankingTransaction transaction;

    @Column(name = "log_entry_text", nullable = false, columnDefinition = "TEXT")
    private String logEntryText;

    @Column(name = "time_stamp", nullable = false)
    private Instant timeStamp;

    protected TransactionLog() {}
}