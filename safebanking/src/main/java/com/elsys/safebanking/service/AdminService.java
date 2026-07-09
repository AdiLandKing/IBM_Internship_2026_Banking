package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.AdminUserResponse;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public AdminService(UserRepository userRepository, BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(user -> {
                long count = bankAccountRepository.countByOwner(user);
                return AdminUserResponse.from(user, count);
            });
    }
}