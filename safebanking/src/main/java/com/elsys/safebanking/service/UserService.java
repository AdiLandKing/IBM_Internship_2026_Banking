package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.ChangeEPinRequest;
import com.elsys.safebanking.dto.EPinResponse;
import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.exception.InvalidCredentialsException;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EPinCipher ePinCipher;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EPinCipher ePinCipher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.ePinCipher = ePinCipher;
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

    @Transactional
    public EPinResponse getEPin(String email) {
        User user = getByEmail(email);
        if (user.getEPin() == null) {
            user.updateEPin(ePinCipher.encrypt(generateEPin()));
        }
        return new EPinResponse(ePinCipher.decrypt(user.getEPin()));
    }

    @Transactional
    public EPinResponse changeEPin(String email, ChangeEPinRequest request) {
        User user = getByEmail(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.updateEPin(ePinCipher.encrypt(request.newEPin()));
        return new EPinResponse(request.newEPin());
    }

    public static String resolveEPin(String requestedEPin) {
        return requestedEPin == null || requestedEPin.isBlank()
                ? generateEPin()
                : requestedEPin;
    }

    private static String generateEPin() {
        return "%06d".formatted(SECURE_RANDOM.nextInt(1_000_000));
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
