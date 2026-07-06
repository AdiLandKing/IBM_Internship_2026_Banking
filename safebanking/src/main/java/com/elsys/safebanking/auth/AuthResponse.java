package com.elsys.safebanking.auth;

import com.elsys.safebanking.user.UserProfileResponse;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        UserProfileResponse user
) {
}
