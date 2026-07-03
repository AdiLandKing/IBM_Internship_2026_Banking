package com.elsys.safebanking.dto;

/**
 * Response body returned after a successful login or register.
 * The actual JWT is stored in the HttpOnly cookie — this body carries
 * only non-sensitive confirmation data so the frontend knows who logged in.
 */
public record AuthResponse(String username, String message) {}
