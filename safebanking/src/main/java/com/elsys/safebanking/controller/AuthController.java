package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.AuthResponse;
import com.elsys.safebanking.dto.LoginRequest;
import com.elsys.safebanking.dto.RegisterRequest;
import com.elsys.safebanking.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * Public authentication endpoints.
 *
 * POST /auth/register  — create account, receive JWT cookie
 * POST /auth/login     — authenticate, receive JWT cookie
 * POST /auth/logout    — clear JWT cookie
 *
 * The JWT itself is never part of the response body; it travels exclusively
 * via the HttpOnly Set-Cookie header.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        try {
            String username = authService.register(request, response);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AuthResponse(username, "Registration successful."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {
        try {
            String username = authService.login(request, response);
            return ResponseEntity.ok(new AuthResponse(username, "Login successful."));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid credentials."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok(new AuthResponse(null, "Logged out successfully."));
    }
}
