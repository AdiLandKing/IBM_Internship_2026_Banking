package com.elsys.safebanking.dto;

import com.elsys.safebanking.dto.UserProfileResponse;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        UserProfileResponse user
) {
}
