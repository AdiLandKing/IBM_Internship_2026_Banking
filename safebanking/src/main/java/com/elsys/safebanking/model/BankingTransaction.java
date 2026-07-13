package com.elsys.safebanking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
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

    /** Amount debited from the source account, in source-account currency. */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /** Amount credited to the destination account, in destination-account currency. */
    @Column(name = "credited_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditedAmount;

    /** ISO-4217 currency of the source account at the time of the transfer. */
    @Column(name = "source_currency", nullable = false, length = 3)
    private String sourceCurrency;

    /** ISO-4217 currency of the destination account at the time of the transfer. */
    @Column(name = "destination_currency", nullable = false, length = 3)
    private String destinationCurrency;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, name = "time_stamp")
    private Instant timeStamp;

    /**
     * FX rate applied: destinationCurrency / sourceCurrency.
     * 1.0 for same-currency transfers.
     */
    @Column(name = "exchange_rate_used", nullable = false, precision = 18, scale = 6)
    private BigDecimal exchangeRateUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    protected BankingTransaction() {
    }

    private BankingTransaction(Builder builder) {
        this.tranId = builder.tranId;
        this.sourceAccount = builder.sourceAccount;
        this.destinationAccount = builder.destinationAccount;
        this.amount = builder.amount;
        this.creditedAmount = builder.creditedAmount;
        this.sourceCurrency = builder.sourceCurrency;
        this.destinationCurrency = builder.destinationCurrency;
        this.reason = builder.reason;
        this.timeStamp = builder.timeStamp;
        this.exchangeRateUsed = builder.exchangeRateUsed;
        this.status = builder.status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long tranId;
        private BankAccount sourceAccount;
        private BankAccount destinationAccount;
        private BigDecimal amount;
        private BigDecimal creditedAmount;
        private String sourceCurrency;
        private String destinationCurrency;
        private String reason;
        private Instant timeStamp;
        private BigDecimal exchangeRateUsed;
        private TransactionStatus status;

        public Builder tranId(Long tranId) {
            this.tranId = tranId;
            return this;
        }

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

        public Builder debitedAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder creditedAmount(BigDecimal creditedAmount) {
            this.creditedAmount = creditedAmount;
            return this;
        }

        public Builder sourceCurrency(String sourceCurrency) {
            this.sourceCurrency = sourceCurrency;
            return this;
        }

        public Builder destinationCurrency(String destinationCurrency) {
            this.destinationCurrency = destinationCurrency;
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
            return new BankingTransaction(this);
        }
    }
}