package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.ChangeEPinRequest;
import com.elsys.safebanking.dto.EPinStatusResponse;
import com.elsys.safebanking.dto.SetEPinRequest;
import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.exception.EPinAlreadySetException;
import com.elsys.safebanking.exception.EPinReuseException;
import com.elsys.safebanking.exception.EPinVerificationException;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.UserRepository;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    @Transactional(readOnly = true)
    public EPinStatusResponse getEPinStatus(String email) {
        User user = getByEmail(email);
        return new EPinStatusResponse(hasUsableEPinHash(user));
    }

    @Transactional
    public EPinStatusResponse setEPin(String email, SetEPinRequest request) {
        User user = getByEmail(email);
        if (hasUsableEPinHash(user)) {
            throw new EPinAlreadySetException();
        }
        verifyPassword(user, request.currentPassword());
        user.updateEPinHash(passwordEncoder.encode(request.newEPin()));
        return new EPinStatusResponse(true);
    }

    @Transactional
    public EPinStatusResponse changeEPin(String email, ChangeEPinRequest request) {
        User user = getByEmail(email);
        verifyChangeCredentials(user, request);
        if (passwordEncoder.matches(request.newEPin(), user.getEPinHash())) {
            throw new EPinReuseException();
        }
        user.updateEPinHash(passwordEncoder.encode(request.newEPin()));
        return new EPinStatusResponse(true);
    }

    private void verifyChangeCredentials(User user, ChangeEPinRequest request) {
        boolean passwordMatches = passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        );
        boolean ePinMatches = hasUsableEPinHash(user)
                && passwordEncoder.matches(request.currentEPin(), user.getEPinHash());

        if (!passwordMatches || !ePinMatches) {
            throw new EPinVerificationException();
        }
    }

    private void verifyPassword(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new EPinVerificationException();
        }
    }

    private boolean hasUsableEPinHash(User user) {
        return user.getEPinHash() != null && BCRYPT_HASH.matcher(user.getEPinHash()).matches();
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
