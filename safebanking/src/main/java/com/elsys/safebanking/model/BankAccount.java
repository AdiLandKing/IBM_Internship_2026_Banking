package com.elsys.safebanking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    @Column(nullable = false, length = 80)
    private String name;

    /*
     * account_name is an old local-schema column. New writes use name only; this
     * read-only field lets older rows display correctly until they are saved again.
     */
    @Column(name = "account_name", insertable = false, updatable = false, length = 80)
    private String legacyAccountName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'ACTIVE'")
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    protected BankAccount() {
    }

    public BankAccount(String iban, String name, String currency, User owner) {
        this(name, iban, BigDecimal.ZERO, currency, owner);
    }

    public BankAccount(String name, String iban, BigDecimal balance, String currency, User owner) {
        this.iban = iban;
        this.name = requireAccountName(name);
        this.balance = balance == null ? BigDecimal.ZERO : balance;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.owner = owner;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLegacyAccountName() {
        return legacyAccountName;
    }

    public void setLegacyAccountName(String legacyAccountName) {
        this.legacyAccountName = legacyAccountName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void updateName(String name) {
        this.name = requireAccountName(name);
    }

    @PostLoad
    private void hydrateLegacyFields() {
        if (isBlank(name) && !isBlank(legacyAccountName)) {
            name = legacyAccountName;
        }
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }

    @PrePersist
    @PreUpdate
    private void validateRequiredFields() {
        name = requireAccountName(name);
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }

    private static String requireAccountName(String value) {
        if (isBlank(value)) {
            throw new IllegalArgumentException("Account name is required");
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public void block() {
        this.status = AccountStatus.BLOCKED;
    }

    public void unblock() {
        this.status = AccountStatus.ACTIVE;
    }
}
