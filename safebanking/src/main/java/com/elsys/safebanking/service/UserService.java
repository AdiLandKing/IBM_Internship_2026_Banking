package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.ChangeEPinRequest;
import com.elsys.safebanking.dto.EPinResponse;
import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.exception.EPinReuseException;
import com.elsys.safebanking.exception.EPinVerificationException;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.UserRepository;
import com.elsys.safebanking.validation.EPinPolicy;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

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
        return new EPinResponse(getOrCreatePlainEPin(user));
    }

    @Transactional
    public EPinResponse changeEPin(String email, ChangeEPinRequest request) {
        User user = getByEmail(email);
        String currentEPin = verifyChangeCredentials(user, request);

        if (EPinPolicy.matches(currentEPin, request.newEPin())) {
            throw new EPinReuseException();
        }
        user.updateEPin(ePinCipher.encrypt(request.newEPin()));
        return new EPinResponse(request.newEPin());
    }

    private String getOrCreatePlainEPin(User user) {
        if (user.getEPin() != null) {
            return ePinCipher.decrypt(user.getEPin());
        }

        String generatedEPin = EPinPolicy.generate();
        user.updateEPin(ePinCipher.encrypt(generatedEPin));
        return generatedEPin;
    }

    private String verifyChangeCredentials(User user, ChangeEPinRequest request) {
        boolean passwordMatches = passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        );
        String currentEPin = user.getEPin() == null
                ? null
                : ePinCipher.decrypt(user.getEPin());
        boolean ePinMatches = EPinPolicy.matches(currentEPin, request.currentEPin());

        if (!passwordMatches || !ePinMatches) {
            throw new EPinVerificationException();
        }
        return currentEPin;
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
