package com.elsys.safebanking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

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

    @Test
    void savePersistsAccountWithIbanPrimaryKey() {
        BankAccount saved = bankAccountRepository.save(new BankAccount(
                "BG4K82L9P01M7Q3X5Z",
                "Savings",
                "EUR",
                userA
        ));

        assertThat(saved.getIban()).isEqualTo("BG4K82L9P01M7Q3X5Z");
        assertThat(saved.getName()).isEqualTo("Savings");
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getOwner().getId()).isEqualTo(userA.getId());
    }

    @Test
    void findByIdUsesIban() {
        bankAccountRepository.save(new BankAccount("BG33BUKB2020155555", "Current", "USD", userA));

        Optional<BankAccount> found = bankAccountRepository.findByIban("BG33BUKB2020155555");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Current");
    }

    @Test
    void findByOwnerIdOrderByIbanAscReturnsOnlyOwnedAccounts() {
        bankAccountRepository.save(new BankAccount("BG11NWBK6016133192", "Savings", "EUR", userA));
        bankAccountRepository.save(new BankAccount("BG22NWBK6016133192", "Current", "USD", userA));
        bankAccountRepository.save(new BankAccount("BG33NWBK6016133192", "Business", "GBP", userB));

        List<BankAccount> results = bankAccountRepository.findByOwnerIdOrderByIbanAsc(userA.getId());

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(BankAccount::getName)
                .containsExactlyInAnyOrder("Savings", "Current");
    }

    @Test
    void findByIbanAndOwnerIdEnforcesOwnership() {
        bankAccountRepository.save(new BankAccount("BG44NWBK6016133192", "Savings", "EUR", userA));

        assertThat(bankAccountRepository.findByIbanAndOwnerId("BG44NWBK6016133192", userA.getId())).isPresent();
        assertThat(bankAccountRepository.findByIbanAndOwnerId("BG44NWBK6016133192", userB.getId())).isEmpty();
    }

    @Test
    void updateNamePersistsNewValue() {
        BankAccount account = bankAccountRepository.save(new BankAccount("BG66NWBK6016133192", "Savings", "EUR", userA));

        account.updateName("Premium Savings");
        bankAccountRepository.save(account);

    BankAccount reloaded = bankAccountRepository.findByIban(account.getIban()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Premium Savings");
    }

    @Test
    void deleteRemovesAccountByIban() {
        BankAccount account = bankAccountRepository.save(new BankAccount("BG77NWBK6016133192", "Temp", "BGN", userA));

    bankAccountRepository.deleteByIban(account.getIban());
        assertThat(bankAccountRepository.findByIban(account.getIban())).isEmpty();
    }

    @Test
    void existsByIbanDetectsDuplicateIban() {
        bankAccountRepository.save(new BankAccount("BG88NWBK6016133192", "Savings", "EUR", userA));

        assertThat(bankAccountRepository.existsByIban("BG88NWBK6016133192")).isTrue();
        assertThat(bankAccountRepository.existsByIban("BG99NWBK6016133192")).isFalse();
    }
}
