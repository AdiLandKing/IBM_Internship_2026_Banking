package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.AuthResponse;
import com.elsys.safebanking.dto.LoginRequest;
import com.elsys.safebanking.dto.RegisterRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.exception.DuplicateEmailException;
import com.elsys.safebanking.exception.InvalidCredentialsException;
import com.elsys.safebanking.model.Users;
import com.elsys.safebanking.repository.UserRepository;
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

        Users user = userRepository.save(new Users(
                email,
                passwordEncoder.encode(request.password()),
                request.firstName().trim(),
                request.lastName().trim()
        ));

        return createAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = UserService.normalizeEmail(request.email());
        Users user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(Users user) {
        String token = jwtService.createToken(user.getEmail());
        return new AuthResponse(
                "Bearer",
                token,
                jwtService.expirationSeconds(),
                UserProfileResponse.from(user)
        );
    }
}
