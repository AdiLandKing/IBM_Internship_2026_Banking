package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.StripeDeposit;
import com.elsys.safebanking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JPA slice test for {@link StripeDepositRepository}.
 * Uses H2 in-memory database (PostgreSQL compatibility mode) from application-test.properties.
 */
@DataJpaTest
@ActiveProfiles("test")
class StripeDepositRepositoryTest {

    @Autowired private StripeDepositRepository stripeDepositRepository;
    @Autowired private BankAccountRepository   bankAccountRepository;
    @Autowired private UserRepository          userRepository;

    private BankAccount account;

    @BeforeEach
    void setUp() {
        stripeDepositRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        User owner = userRepository.save(new User("alice@example.com", "hash", "Alice", "Smith"));
        account = bankAccountRepository.save(
                new BankAccount("Savings", "GB29NWBK60161331926819", new BigDecimal("500.00"), "eur", owner));
    }

    // =========================================================================
    // existsByStripePaymentIntentId
    // =========================================================================

    @Test
    void existsByStripePaymentIntentId_returnsFalse_whenNoMatchingRecord() {
        assertThat(stripeDepositRepository.existsByStripePaymentIntentId("pi_unknown")).isFalse();
    }

    @Test
    void existsByStripePaymentIntentId_returnsTrue_afterSave() {
        stripeDepositRepository.save(deposit("pi_test_001", new BigDecimal("10.00")));

        assertThat(stripeDepositRepository.existsByStripePaymentIntentId("pi_test_001")).isTrue();
    }

    @Test
    void existsByStripePaymentIntentId_returnsFalse_forDifferentId() {
        stripeDepositRepository.save(deposit("pi_test_002", new BigDecimal("10.00")));

        assertThat(stripeDepositRepository.existsByStripePaymentIntentId("pi_test_001")).isFalse();
    }

    // =========================================================================
    // UNIQUE constraint — idempotency guard
    // =========================================================================

    @Test
    void save_throwsDataIntegrityViolation_onDuplicatePaymentIntentId() {
        stripeDepositRepository.save(deposit("pi_dupe_001", new BigDecimal("10.00")));
        stripeDepositRepository.flush(); // ensure first write hits the DB

        assertThatThrownBy(() -> {
            stripeDepositRepository.save(deposit("pi_dupe_001", new BigDecimal("5.00")));
            stripeDepositRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    // =========================================================================
    // basic persistence
    // =========================================================================

    @Test
    void save_persistsAllFields() {
        Instant before = Instant.now();
        StripeDeposit saved = stripeDepositRepository.save(
                deposit("pi_fields_001", new BigDecimal("25.50")));

        assertThat(saved.getId()).isNotNull();

        StripeDeposit reloaded = stripeDepositRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStripePaymentIntentId()).isEqualTo("pi_fields_001");
        assertThat(reloaded.getAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(reloaded.getCurrency()).isEqualTo("eur");
        assertThat(reloaded.getCreatedAt()).isAfterOrEqualTo(before);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private StripeDeposit deposit(String intentId, BigDecimal amount) {
        return new StripeDeposit(intentId, account, amount, "eur", Instant.now());
    }
}
