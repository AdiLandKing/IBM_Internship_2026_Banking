package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.AdminUserResponse;
import com.elsys.safebanking.dto.ChangeEPinRequest;
import com.elsys.safebanking.dto.EPinStatusResponse;
import com.elsys.safebanking.dto.SetEPinRequest;
import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.exception.EPinAlreadySetException;
import com.elsys.safebanking.exception.EPinReuseException;
import com.elsys.safebanking.exception.EPinVerificationException;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.BankAccountRepository;
import com.elsys.safebanking.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EPinAttemptLimiter ePinAttemptLimiter;

    public UserService(
            UserRepository userRepository,
            BankAccountRepository bankAccountRepository,
            PasswordEncoder passwordEncoder,
            EPinAttemptLimiter ePinAttemptLimiter
    ) {
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.ePinAttemptLimiter = ePinAttemptLimiter;
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

    @Transactional(readOnly = true)
    public EPinStatusResponse getEPinStatus(String email) {
        User user = getByEmail(email);
        return new EPinStatusResponse(hasUsableEPinHash(user));
    }

    @Transactional
    public EPinStatusResponse setEPin(String email, SetEPinRequest request, String clientIp) {
        User user = getByEmail(email);
        checkEPinAllowed(user, clientIp, "set");
        auditEPinAttempt(user, clientIp, "set");
        if (hasUsableEPinHash(user)) {
            auditEPinFailure(user, clientIp, "set", "already_set");
            ePinAttemptLimiter.recordFailure(user.getId(), clientIp, "set");
            throw new EPinAlreadySetException();
        }
        verifyPassword(user, request.currentPassword(), clientIp, "set");
        user.updateEPinHash(passwordEncoder.encode(request.newEPin()));
        ePinAttemptLimiter.recordSuccess(user.getId(), clientIp, "set");
        auditEPinSuccess(user, clientIp, "set");
        return new EPinStatusResponse(true);
    }

    @Transactional
    public EPinStatusResponse changeEPin(String email, ChangeEPinRequest request, String clientIp) {
        User user = getByEmail(email);
        checkEPinAllowed(user, clientIp, "change");
        auditEPinAttempt(user, clientIp, "change");
        verifyChangeCredentials(user, request, clientIp);
        if (passwordEncoder.matches(request.newEPin(), user.getEPinHash())) {
            auditEPinFailure(user, clientIp, "change", "reused_pin");
            ePinAttemptLimiter.recordFailure(user.getId(), clientIp, "change");
            throw new EPinReuseException();
        }
        user.updateEPinHash(passwordEncoder.encode(request.newEPin()));
        ePinAttemptLimiter.recordSuccess(user.getId(), clientIp, "change");
        auditEPinSuccess(user, clientIp, "change");
        return new EPinStatusResponse(true);
    }

    private void verifyChangeCredentials(User user, ChangeEPinRequest request, String clientIp) {
        boolean passwordMatches = passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash()
        );
        boolean ePinMatches = hasUsableEPinHash(user)
                && passwordEncoder.matches(request.currentEPin(), user.getEPinHash());

        if (!passwordMatches || !ePinMatches) {
            auditEPinFailure(user, clientIp, "change", "invalid_credentials");
            ePinAttemptLimiter.recordFailure(user.getId(), clientIp, "change");
            throw new EPinVerificationException();
        }
    }

    private void verifyPassword(User user, String currentPassword, String clientIp, String action) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            auditEPinFailure(user, clientIp, action, "invalid_credentials");
            ePinAttemptLimiter.recordFailure(user.getId(), clientIp, action);
            throw new EPinVerificationException();
        }
    }

    private boolean hasUsableEPinHash(User user) {
        // Values that are not bcrypt hashes are legacy reversible E-PIN values.
        // Treat them as unset so users must replace them with the current hashed format.
        return user.getEPinHash() != null && BCRYPT_HASH.matcher(user.getEPinHash()).matches();
    }

    private void checkEPinAllowed(User user, String clientIp, String action) {
        try {
            ePinAttemptLimiter.checkAllowed(user.getId(), clientIp, action);
        } catch (RuntimeException exception) {
            auditEPinFailure(user, clientIp, action, "rate_limited");
            throw exception;
        }
    }

    private void auditEPinAttempt(User user, String clientIp, String action) {
        log.info(
                "E-PIN {} attempt userId={} ip={} at={}",
                action,
                user.getId(),
                clientIp,
                Instant.now()
        );
    }

    private void auditEPinSuccess(User user, String clientIp, String action) {
        log.info(
                "E-PIN {} success userId={} ip={} at={}",
                action,
                user.getId(),
                clientIp,
                Instant.now()
        );
    }

    private void auditEPinFailure(User user, String clientIp, String action, String reason) {
        log.warn(
                "E-PIN {} failure userId={} ip={} reason={} at={}",
                action,
                user.getId(),
                clientIp,
                reason,
                Instant.now()
        );
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
