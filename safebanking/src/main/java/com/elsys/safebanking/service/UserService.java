package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
