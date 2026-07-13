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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

}