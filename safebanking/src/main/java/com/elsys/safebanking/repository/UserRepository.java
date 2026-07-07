package com.elsys.safebanking.repository;

import com.elsys.safebanking.model.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
