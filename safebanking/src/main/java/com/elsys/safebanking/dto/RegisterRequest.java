package com.elsys.safebanking.dto;

/**
 * Payload for POST /auth/register
 */
public record RegisterRequest(String username, String password) {}
