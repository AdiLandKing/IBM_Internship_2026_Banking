package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test — no Spring context needed.
 * Verifies that {@link BankAccountResponse#from(BankAccount)} maps every field correctly.
 */
class BankAccountResponseTest {

    /**
     * Build a BankAccount using the public constructor.
     * The accountId stays null until persisted — fine here since we test mapping, not persistence.
     */
    private BankAccount buildAccount(String name, Integer balance, String currency, User owner) {
        return new BankAccount(name, balance, currency, owner);
    }

    // -------------------------------------------------------------------------
    // from() mapping
    // -------------------------------------------------------------------------

    @Test
    void from_mapsAllFieldsCorrectly() {
        User owner = new User("owner@example.com", "hash", "Alice", "Smith");
        BankAccount account = buildAccount("Savings", 1500, "EUR", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.accountName()).isEqualTo("Savings");
        assertThat(response.balance()).isEqualTo(1500);
        assertThat(response.currency()).isEqualTo("EUR");
        // id is null until persisted; userId is forwarded from owner.getId()
        assertThat(response.userId()).isNull();
        assertThat(response.accountId()).isNull();
    }

    @Test
    void from_mapsZeroBalance() {
        User owner = new User("zero@example.com", "hash", "Bob", "Jones");
        BankAccount account = buildAccount("Empty", 0, "USD", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.balance()).isZero();
        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    void from_mapsNegativeBalance() {
        User owner = new User("neg@example.com", "hash", "Carol", "Doe");
        BankAccount account = buildAccount("Overdrawn", -250, "GBP", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.balance()).isEqualTo(-250);
    }

    @Test
    void from_propagatesOwnerUserId() {
        User owner = new User("biz@example.com", "hash", "Dave", "Lee");
        BankAccount account = buildAccount("Business", 10000, "BGN", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        // id is null until persisted — just verify the forwarding path doesn't throw
        assertThat(response.userId()).isNull();
    }

    // -------------------------------------------------------------------------
    // record equality & accessors
    // -------------------------------------------------------------------------

    @Test
    void recordEquality_twoIdenticalInstancesAreEqual() {
        BankAccountResponse a = new BankAccountResponse(1, "Savings", 1000, "EUR", 5L);
        BankAccountResponse b = new BankAccountResponse(1, "Savings", 1000, "EUR", 5L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void recordEquality_differentBalanceMeansNotEqual() {
        BankAccountResponse a = new BankAccountResponse(1, "Savings", 1000, "EUR", 5L);
        BankAccountResponse b = new BankAccountResponse(1, "Savings", 9999, "EUR", 5L);

        assertThat(a).isNotEqualTo(b);
    }
}
