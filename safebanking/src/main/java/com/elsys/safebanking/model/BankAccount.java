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

@Getter
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    protected BankAccount() {
    }

    public BankAccount(String accountName, Integer balance, String currency, User owner) {
        this.accountName = accountName;
        this.balance = balance;
        this.currency = currency;
        this.owner = owner;
    }

    public void updateBalance(Integer balance) {
        this.balance = balance;
    }

    public void updateAccountName(String accountName) {
        this.accountName = accountName;
    }
}
