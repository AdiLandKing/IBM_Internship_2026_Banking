package com.elsys.safebanking.dto;

/**
 * Payload for POST /auth/login
 */
public record LoginRequest(String username, String password) {}
