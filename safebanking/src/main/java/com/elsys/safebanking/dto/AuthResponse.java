package com.elsys.safebanking.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        UserProfileResponse user
) {
}
