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
import lombok.Getter;

@Getter
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @Column(nullable = false, unique = true, length = 18)
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
        this.iban = iban;
        this.name = requireAccountName(name);
        this.balance = BigDecimal.ZERO;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.owner = owner;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void updateName(String name) {
        this.name = requireAccountName(name);
    }

    public void suspend() {
        status = AccountStatus.SUSPENDED;
    }

    public void activate() {
        status = AccountStatus.ACTIVE;
    }

    public void block() {
        status = AccountStatus.BLOCKED;
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
}
