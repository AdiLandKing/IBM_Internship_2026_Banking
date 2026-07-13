package com.elsys.safebanking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.elsys.safebanking.model.AccountStatus;
import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BankAccountResponseTest {

    @Test
    void fromMapsAllPublicFields() {
        User owner = new User("owner@example.com", "hash", "Alice", "Smith");
        BankAccount account = new BankAccount("BG4K82L9P01M7Q3X5Z", "Main Account", "BGN", owner);

        BankAccountResponse response = BankAccountResponse.from(account);

        assertThat(response.iban()).isEqualTo("BG4K82L9P01M7Q3X5Z");
        assertThat(response.name()).isEqualTo("Main Account");
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.currency()).isEqualTo("BGN");
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.createdAt()).isNull();
    }

    @Test
    void recordEqualityUsesAllFields() {
        Instant createdAt = Instant.parse("2026-07-13T08:00:00Z");
        BankAccountResponse first = new BankAccountResponse(
                "BG4K82L9P01M7Q3X5Z",
                "Main Account",
                BigDecimal.ZERO,
                "BGN",
                AccountStatus.ACTIVE,
                createdAt
        );
        BankAccountResponse second = new BankAccountResponse(
                "BG4K82L9P01M7Q3X5Z",
                "Main Account",
                BigDecimal.ZERO,
                "BGN",
                AccountStatus.ACTIVE,
                createdAt
        );

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
