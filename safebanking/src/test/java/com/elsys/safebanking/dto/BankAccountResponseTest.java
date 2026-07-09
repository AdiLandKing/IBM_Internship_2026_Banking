package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
    private BankAccount buildAccount(String name, BigDecimal balance, String currency, User owner) {
        return new BankAccount(name, "GB29NWBK60161331926819", balance, currency, owner);
    }

    // -------------------------------------------------------------------------
    // from() mapping
    // -------------------------------------------------------------------------

    @Test
    void from_mapsAllFieldsCorrectly() {
        User owner = new User("owner@example.com", "hash", "Alice", "Smith");
        BankAccount account = buildAccount("Savings", new BigDecimal("1500.00"), "EUR", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.accountName()).isEqualTo("Savings");
        assertThat(response.iban()).isEqualTo("GB29NWBK60161331926819");
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.accountId()).isNull();
    }

    @Test
    void from_mapsZeroBalance() {
        User owner = new User("zero@example.com", "hash", "Bob", "Jones");
        BankAccount account = buildAccount("Empty", BigDecimal.ZERO, "USD", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    void from_mapsNegativeBalance() {
        User owner = new User("neg@example.com", "hash", "Carol", "Doe");
        BankAccount account = buildAccount("Overdrawn", new BigDecimal("-250.00"), "GBP", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("-250.00"));
    }

    @Test
    void from_propagatesIban() {
        User owner = new User("biz@example.com", "hash", "Dave", "Lee");
        BankAccount account = buildAccount("Business", new BigDecimal("10000.00"), "BGN", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.iban()).isEqualTo("GB29NWBK60161331926819");
    }

    // -------------------------------------------------------------------------
    // record equality & accessors
    // -------------------------------------------------------------------------

    @Test
    void recordEquality_twoIdenticalInstancesAreEqual() {
        BankAccountResponse a = new BankAccountResponse(1, "Savings", "GB29NWBK60161331926819", new BigDecimal("1000.00"), "EUR");
        BankAccountResponse b = new BankAccountResponse(1, "Savings", "GB29NWBK60161331926819", new BigDecimal("1000.00"), "EUR");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void recordEquality_differentBalanceMeansNotEqual() {
        BankAccountResponse a = new BankAccountResponse(1, "Savings", "GB29NWBK60161331926819", new BigDecimal("1000.00"), "EUR");
        BankAccountResponse b = new BankAccountResponse(1, "Savings", "GB29NWBK60161331926819", new BigDecimal("9999.00"), "EUR");

        assertThat(a).isNotEqualTo(b);
    }
}
