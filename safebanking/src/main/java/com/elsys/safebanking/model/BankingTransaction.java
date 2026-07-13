package com.elsys.safebanking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class BankingTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tranId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_iban", referencedColumnName = "iban", nullable = false)
    private BankAccount sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_iban", referencedColumnName = "iban", nullable = false)
    private BankAccount destinationAccount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, name = "time_stamp")
    private Instant timeStamp;

    @Column(name = "exchange_rate_used", precision = 18, scale = 4)
    private BigDecimal exchangeRateUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    public BankingTransaction() {
    }

    public BankingTransaction(Long tranId, BankAccount sourceAccount, BankAccount destinationAccount, BigDecimal amount, String reason, Instant timeStamp, BigDecimal exchangeRateUsed, TransactionStatus status) {
        this.tranId = tranId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.reason = reason;
        this.timeStamp = timeStamp;
        this.exchangeRateUsed = exchangeRateUsed;
        this.status = status;
    }

    public Long getTranId() {
        return tranId;
    }

    public BankAccount getSourceAccount() {
        return sourceAccount;
    }

    public BankAccount getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public BigDecimal getExchangeRateUsed() {
        return exchangeRateUsed;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BankAccount sourceAccount;
        private BankAccount destinationAccount;
        private BigDecimal amount;
        private String reason;
        private Instant timeStamp;
        private BigDecimal exchangeRateUsed;
        private TransactionStatus status;

        public Builder sourceAccount(BankAccount sourceAccount) {
            this.sourceAccount = sourceAccount;
            return this;
        }

        public Builder destinationAccount(BankAccount destinationAccount) {
            this.destinationAccount = destinationAccount;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder timeStamp(Instant timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public Builder exchangeRateUsed(BigDecimal exchangeRateUsed) {
            this.exchangeRateUsed = exchangeRateUsed;
            return this;
        }

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public BankingTransaction build() {
            return new BankingTransaction(null, sourceAccount, destinationAccount, amount, reason, timeStamp, exchangeRateUsed, status);
        }
    }
}