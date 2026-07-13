package com.elsys.safebanking.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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