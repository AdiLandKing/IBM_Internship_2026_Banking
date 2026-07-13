package com.elsys.safebanking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transaction_logs")
@Getter
@Setter
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

    protected TransactionLog() {
    }

    private TransactionLog(Builder builder) {
        this.logId = builder.logId;
        this.transaction = builder.transaction;
        this.logEntryText = builder.logEntryText;
        this.timeStamp = builder.timeStamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long logId;
        private BankingTransaction transaction;
        private String logEntryText;
        private Instant timeStamp;

        public Builder logId(Long logId) {
            this.logId = logId;
            return this;
        }

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
            return new TransactionLog(this);
        }
    }
}