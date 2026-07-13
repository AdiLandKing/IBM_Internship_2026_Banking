package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.BankAccount;
import com.elsys.safebanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    
    Optional<BankAccount> findByIban(String iban);
    long countByOwner(User owner);
    List<BankAccount> findByOwnerId(Long ownerId);

    boolean existsByIban(String iban);
    List<BankAccount> findByOwnerIdOrderByIbanAsc(Long ownerId);
    Optional<BankAccount> findByIbanAndOwnerId(String iban, Long ownerId);
    void deleteByIban(String iban);
}
