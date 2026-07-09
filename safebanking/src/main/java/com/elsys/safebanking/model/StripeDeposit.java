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
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Records a successfully processed Stripe top-up event.
 * The UNIQUE constraint on stripePaymentIntentId provides idempotency:
 * if Stripe retries the same webhook, the DB write fails and the balance
 * is never credited twice.
 */
@Getter
@Entity
@Table(name = "stripe_deposits")
public class StripeDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stripe PaymentIntent id, e.g. "pi_3Abc…". Unique to prevent double-credit. */
    @Column(name = "stripe_payment_intent_id", nullable = false, unique = true, length = 100)
    private String stripePaymentIntentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_iban", nullable = false)
    private BankAccount account;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    protected StripeDeposit() {}

    public StripeDeposit(String stripePaymentIntentId, BankAccount account,
                         BigDecimal amount, String currency, Instant createdAt) {
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = createdAt;
    }
}
