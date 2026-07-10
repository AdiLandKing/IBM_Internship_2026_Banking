
package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    long countByOwner(User owner);

    List<BankAccount> findByOwnerIdOrderByIbanAsc(Long ownerId);

    Optional<BankAccount> findByIbanAndOwnerId(String iban, Long ownerId);

    boolean existsByIban(String iban);
}
