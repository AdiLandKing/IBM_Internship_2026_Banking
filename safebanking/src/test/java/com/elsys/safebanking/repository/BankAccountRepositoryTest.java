package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test — only JPA layer is loaded (H2 in-memory, schema created from entities).
 */
@DataJpaTest
@ActiveProfiles("test")
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(new User("a@example.com", "hash", "Alice", "Smith"));
        userB = userRepository.save(new User("b@example.com", "hash", "Bob", "Jones"));
    }

    // -------------------------------------------------------------------------
    // save & findById
    // -------------------------------------------------------------------------

    @Test
    void save_persistsAccountAndAssignsId() {
        BankAccount account = new BankAccount("Savings", "GB82WEST12345698765432", new BigDecimal("1000.00"), "EUR", userA);

        BankAccount saved = bankAccountRepository.save(account);

        assertThat(saved.getAccountId()).isNotNull();
        assertThat(saved.getAccountName()).isEqualTo("Savings");
        assertThat(saved.getIban()).isEqualTo("GB82WEST12345698765432");
        assertThat(saved.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getOwner().getId()).isEqualTo(userA.getId());
    }

    @Test
    void findById_returnsAccount_whenIdExists() {
        BankAccount saved = bankAccountRepository.save(new BankAccount("Current", "GB33BUKB20201555555555", new BigDecimal("500.00"), "USD", userA));

        Optional<BankAccount> found = bankAccountRepository.findById(saved.getAccountId());

        assertThat(found).isPresent();
        assertThat(found.get().getAccountName()).isEqualTo("Current");
    }

    @Test
    void findById_returnsEmpty_whenIdDoesNotExist() {
        Optional<BankAccount> found = bankAccountRepository.findById(Integer.MAX_VALUE);

        assertThat(found).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findByOwnerId
    // -------------------------------------------------------------------------

    @Test
    void findByOwnerId_returnsAllAccountsForThatUser() {
        bankAccountRepository.save(new BankAccount("Savings",  "GB11NWBK60161331926819", new BigDecimal("1000.00"), "EUR", userA));
        bankAccountRepository.save(new BankAccount("Current",  "GB22NWBK60161331926820", new BigDecimal("200.00"),  "USD", userA));
        bankAccountRepository.save(new BankAccount("Business", "GB33NWBK60161331926821", new BigDecimal("9999.00"), "GBP", userB));

        List<BankAccount> results = bankAccountRepository.findByOwnerId(userA.getId());

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Savings", "Current");
    }

    @Test
    void findByOwnerId_returnsEmpty_whenUserHasNoAccounts() {
        bankAccountRepository.save(new BankAccount("Savings", "GB44NWBK60161331926822", new BigDecimal("500.00"), "EUR", userA));

        List<BankAccount> results = bankAccountRepository.findByOwnerId(userB.getId());

        assertThat(results).isEmpty();
    }

    @Test
    void findByOwnerId_returnsEmpty_whenUserDoesNotExist() {
        List<BankAccount> results = bankAccountRepository.findByOwnerId(Long.MAX_VALUE);

        assertThat(results).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateBalance / updateAccountName (domain mutators)
    // -------------------------------------------------------------------------

    @Test
    void updateBalance_persistsNewValue() {
        BankAccount account = bankAccountRepository.save(new BankAccount("Savings", "GB55NWBK60161331926823", new BigDecimal("1000.00"), "EUR", userA));

        account.updateBalance(new BigDecimal("2500.00"));
        bankAccountRepository.save(account);

        BankAccount reloaded = bankAccountRepository.findById(account.getAccountId()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
    }

    @Test
    void updateAccountName_persistsNewValue() {
        BankAccount account = bankAccountRepository.save(new BankAccount("Savings", "GB66NWBK60161331926824", new BigDecimal("1000.00"), "EUR", userA));

        account.updateAccountName("Premium Savings");
        bankAccountRepository.save(account);

        BankAccount reloaded = bankAccountRepository.findById(account.getAccountId()).orElseThrow();
        assertThat(reloaded.getAccountName()).isEqualTo("Premium Savings");
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_removesAccount() {
        BankAccount account = bankAccountRepository.save(new BankAccount("Temp", "GB77NWBK60161331926825", BigDecimal.ZERO, "BGN", userA));
        Integer id = account.getAccountId();

        bankAccountRepository.deleteById(id);

        assertThat(bankAccountRepository.findById(id)).isEmpty();
    }

    @Test
    void deleteUser_cascadeNotRequired_butOtherUserAccountsRemain() {
        bankAccountRepository.save(new BankAccount("Savings", "GB88NWBK60161331926826", new BigDecimal("100.00"), "EUR", userA));
        bankAccountRepository.save(new BankAccount("Current", "GB99NWBK60161331926827", new BigDecimal("200.00"), "EUR", userB));

        // Delete userA's accounts first, then the user
        bankAccountRepository.deleteAll(bankAccountRepository.findByOwnerId(userA.getId()));
        userRepository.delete(userA);

        assertThat(bankAccountRepository.findByOwnerId(userA.getId())).isEmpty();
        assertThat(bankAccountRepository.findByOwnerId(userB.getId())).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void findAll_returnsEveryAccount() {
        bankAccountRepository.save(new BankAccount("A1", "GB01NWBK60161331926828", new BigDecimal("100.00"), "EUR", userA));
        bankAccountRepository.save(new BankAccount("A2", "GB02NWBK60161331926829", new BigDecimal("200.00"), "EUR", userA));
        bankAccountRepository.save(new BankAccount("B1", "GB03NWBK60161331926830", new BigDecimal("300.00"), "USD", userB));

        List<BankAccount> all = bankAccountRepository.findAll();

        assertThat(all).hasSize(3);
    }
}
