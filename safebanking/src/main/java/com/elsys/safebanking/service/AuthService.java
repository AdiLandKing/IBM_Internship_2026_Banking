package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.AuthResponse;
import com.elsys.safebanking.dto.LoginRequest;
import com.elsys.safebanking.dto.RegisterRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.exception.DuplicateEmailException;
import com.elsys.safebanking.exception.InvalidCredentialsException;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.UserRepository;
import com.elsys.safebanking.validation.EPinPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = UserService.normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        User user = userRepository.save(new User(
                email,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim(),
                request.dateOfBirth()
        ));
        EPinPolicy.Resolution ePinResolution = EPinPolicy.resolve(request.ePin());
        user.updateEPinHash(passwordEncoder.encode(ePinResolution.value()));

        return createAuthResponse(
                user,
                ePinResolution.generated() ? ePinResolution.value() : null
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = UserService.normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return createAuthResponse(user, null);
    }

    private AuthResponse createAuthResponse(User user, String oneTimeEPin) {
        String token = jwtService.createToken(user.getEmail());
        return new AuthResponse(
                "Bearer",
                token,
                jwtService.expirationSeconds(),
                UserProfileResponse.from(user),
                oneTimeEPin
        );
    }
}
