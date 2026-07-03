package com.elsys.safebanking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Example protected endpoint.
 *
 * Requires a valid JWT cookie — the JwtAuthFilter populates the security
 * context before this handler runs.  If the cookie is missing or expired
 * Spring Security returns 401 automatically.
 *
 * GET /api/user/me
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "role",     userDetails.getAuthorities().iterator().next().getAuthority()
        ));
    }
}
