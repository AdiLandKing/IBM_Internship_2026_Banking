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

    public TransactionLog() {
    }

    public TransactionLog(Long logId, BankingTransaction transaction, String logEntryText, Instant timeStamp) {
        this.logId = logId;
        this.transaction = transaction;
        this.logEntryText = logEntryText;
        this.timeStamp = timeStamp;
    }

    public Long getLogId() {
        return logId;
    }

    public BankingTransaction getTransaction() {
        return transaction;
    }

    public String getLogEntryText() {
        return logEntryText;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BankingTransaction transaction;
        private String logEntryText;
        private Instant timeStamp;

        public Builder transaction(BankingTransaction transaction) {
            this.transaction = transaction;
            return this;
        }

        public Builder logEntryText(String logEntryText) {
            this.logEntryText = logEntryText;
            return this;
        }

        public Builder timeStamp(Instant timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public TransactionLog build() {
            return new TransactionLog(null, transaction, logEntryText, timeStamp);
        }
    }
}