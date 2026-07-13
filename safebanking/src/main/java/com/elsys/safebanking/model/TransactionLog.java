package com.elsys.safebanking.model;

import jakarta.persistence.*;
import java.time.Instant;

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

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public BankingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(BankingTransaction transaction) {
        this.transaction = transaction;
    }

    public String getLogEntryText() {
        return logEntryText;
    }

    public void setLogEntryText(String logEntryText) {
        this.logEntryText = logEntryText;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }
}
