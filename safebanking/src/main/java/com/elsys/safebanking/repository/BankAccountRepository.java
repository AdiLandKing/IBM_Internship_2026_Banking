
package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    long countByOwner(User owner);
    List<BankAccount> findByOwnerId(Long ownerId);
}