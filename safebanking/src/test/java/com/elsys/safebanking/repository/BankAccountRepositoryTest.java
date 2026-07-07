package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

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
        BankAccount account = new BankAccount("Savings", 1000, "EUR", userA);

        BankAccount saved = bankAccountRepository.save(account);

        assertThat(saved.getAccountId()).isNotNull();
        assertThat(saved.getAccountName()).isEqualTo("Savings");
        assertThat(saved.getBalance()).isEqualTo(1000);
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getOwner().getId()).isEqualTo(userA.getId());
    }

    @Test
    void findById_returnsAccount_whenIdExists() {
        BankAccount saved = bankAccountRepository.save(new BankAccount("Current", 500, "USD", userA));

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
        bankAccountRepository.save(new BankAccount("Savings",  1000, "EUR", userA));
        bankAccountRepository.save(new BankAccount("Current",   200, "USD", userA));
        bankAccountRepository.save(new BankAccount("Business", 9999, "GBP", userB));

        List<BankAccount> results = bankAccountRepository.findByOwnerId(userA.getId());

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(BankAccount::getAccountName)
                .containsExactlyInAnyOrder("Savings", "Current");
    }

    @Test
    void findByOwnerId_returnsEmpty_whenUserHasNoAccounts() {
        bankAccountRepository.save(new BankAccount("Savings", 500, "EUR", userA));

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
        BankAccount account = bankAccountRepository.save(new BankAccount("Savings", 1000, "EUR", userA));

        account.updateBalance(2500);
        bankAccountRepository.save(account);

        BankAccount reloaded = bankAccountRepository.findById(account.getAccountId()).orElseThrow();
        assertThat(reloaded.getBalance()).isEqualTo(2500);
    }

    @Test
    void updateAccountName_persistsNewValue() {
        BankAccount account = bankAccountRepository.save(new BankAccount("Savings", 1000, "EUR", userA));

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
        BankAccount account = bankAccountRepository.save(new BankAccount("Temp", 0, "BGN", userA));
        Integer id = account.getAccountId();

        bankAccountRepository.deleteById(id);

        assertThat(bankAccountRepository.findById(id)).isEmpty();
    }

    @Test
    void deleteUser_cascadeNotRequired_butOtherUserAccountsRemain() {
        bankAccountRepository.save(new BankAccount("Savings", 100, "EUR", userA));
        bankAccountRepository.save(new BankAccount("Current", 200, "EUR", userB));

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
        bankAccountRepository.save(new BankAccount("A1", 100, "EUR", userA));
        bankAccountRepository.save(new BankAccount("A2", 200, "EUR", userA));
        bankAccountRepository.save(new BankAccount("B1", 300, "USD", userB));

        List<BankAccount> all = bankAccountRepository.findAll();

        assertThat(all).hasSize(3);
    }
}
