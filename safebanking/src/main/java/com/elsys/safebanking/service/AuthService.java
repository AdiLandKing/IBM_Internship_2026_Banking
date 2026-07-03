package com.elsys.safebanking.service;

import com.elsys.safebanking.dto.LoginRequest;
import com.elsys.safebanking.dto.RegisterRequest;
import com.elsys.safebanking.model.User;
import com.elsys.safebanking.repository.UserRepository;
import com.elsys.safebanking.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles the register and login business logic.
 *
 * On success it writes the JWT into an HttpOnly, Secure, SameSite=Strict
 * cookie on the outgoing response.  The token value never appears in the
 * response body, so JavaScript on the frontend can never read it.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final long cookieMaxAgeSeconds;

    public AuthService(
            UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            @Value("${jwt.expiration-ms}") long expirationMs) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        // Convert milliseconds to seconds for the cookie Max-Age attribute.
        this.cookieMaxAgeSeconds = expirationMs / 1000;
    }

    // ---- Register -----------------------------------------------------------

    /**
     * Creates a new user and immediately issues a JWT cookie so the user
     * is logged in straight after registration.
     *
     * @throws IllegalArgumentException if the username is already taken.
     */
    public String register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already in use.");
        }

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                "ROLE_USER"
        );
        userRepository.save(user);

        writeJwtCookie(jwtService.generateToken(user), response);
        return user.getUsername();
    }

    // ---- Login --------------------------------------------------------------

    /**
     * Authenticates the user via Spring Security's AuthenticationManager
     * (which compares the BCrypt hash), then issues a fresh JWT cookie.
     *
     * @throws org.springframework.security.core.AuthenticationException on bad credentials.
     */
    public String login(LoginRequest request, HttpServletResponse response) {
        // This call throws BadCredentialsException if credentials are wrong.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(); // can't be absent — authentication above already passed

        writeJwtCookie(jwtService.generateToken(user), response);
        return user.getUsername();
    }

    // ---- Logout -------------------------------------------------------------

    /**
     * Clears the JWT cookie by overwriting it with an empty, zero-max-age value.
     */
    public void logout(HttpServletResponse response) {
        Cookie expiredCookie = buildJwtCookie("", 0);
        response.addCookie(expiredCookie);
    }

    // ---- Cookie factory -----------------------------------------------------

    private void writeJwtCookie(String token, HttpServletResponse response) {
        response.addCookie(buildJwtCookie(token, (int) cookieMaxAgeSeconds));
    }

    /**
     * Builds a cookie with the three security flags that protect the JWT:
     *
     * <ul>
     *   <li><b>HttpOnly</b> — JavaScript (including XSS payloads) cannot read it.</li>
     *   <li><b>Secure</b>   — Browser only sends it over HTTPS.</li>
     *   <li><b>SameSite=Strict</b> — Cookie is never sent on cross-site requests,
     *       preventing CSRF attacks even without a CSRF token.</li>
     * </ul>
     */
    private Cookie buildJwtCookie(String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie("jwt", value);
        cookie.setHttpOnly(true);   // Hidden from JavaScript
        cookie.setSecure(true);     // HTTPS only
        cookie.setPath("/");        // Available to all endpoints
        cookie.setMaxAge(maxAgeSeconds);

        // Jakarta Servlet 6 (shipped with Spring Boot 3+) exposes setAttribute
        // for the SameSite directive, which is not available via the older API.
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }
}
