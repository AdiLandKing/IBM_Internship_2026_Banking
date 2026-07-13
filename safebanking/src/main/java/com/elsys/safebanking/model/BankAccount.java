package com.elsys.safebanking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @Column(nullable = false, unique = true, length = 18)
    private String iban;

    @Column(name = "account_name", nullable = false, length = 80)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    protected BankAccount() {
    }

    public BankAccount(String iban, String name, String currency, User owner) {
        this.iban = iban;
        this.name = name;
        this.balance = BigDecimal.ZERO;
        this.currency = currency;
        this.owner = owner;
    }

    public String getIban() {
        return iban;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public User getOwner() {
        return owner;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
