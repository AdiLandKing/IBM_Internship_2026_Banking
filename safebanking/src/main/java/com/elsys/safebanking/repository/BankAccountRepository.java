package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {

    List<BankAccount> findByOwnerId(Long userId);

    Optional<BankAccount> findByIban(String iban);
}
