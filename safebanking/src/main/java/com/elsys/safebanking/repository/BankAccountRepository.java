package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {

    Optional<BankAccount> findByIban(String iban);
    List<BankAccount> findByOwnerId(Long userId);

}
