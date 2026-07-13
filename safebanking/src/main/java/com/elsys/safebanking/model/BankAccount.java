package com.elsys.safebanking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
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

    @Column(name = "account_name", nullable = false, length = 80)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Version
    private Long version;

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
}
