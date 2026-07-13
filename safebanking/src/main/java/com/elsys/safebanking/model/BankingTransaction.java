package com.elsys.safebanking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, name = "exchange_rate_used", precision = 18, scale = 6)
    private BigDecimal exchangeRateUsed;

    protected BankingTransaction() {}
}
