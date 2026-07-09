package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.AdminUserResponse;
import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserService(UserRepository userRepository, BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(user -> {
                long count = bankAccountRepository.countByOwner(user);
                return AdminUserResponse.from(user, count);
            });
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getByEmail(email);
        user.updateProfile(request.firstName().trim(), request.lastName().trim());
        return UserProfileResponse.from(user);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}