package com.elsys.safebanking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @Column(nullable = false, unique = true, length = 18)
    private String iban;

    @Column(length = 80)
    private String name;

    @Column(name = "account_name", insertable = false, updatable = false, length = 80)
    private String legacyAccountName;

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

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public String getName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return legacyAccountName;
    }

    public AccountStatus getStatus() {
        return AccountStatus.ACTIVE;
    }
}
