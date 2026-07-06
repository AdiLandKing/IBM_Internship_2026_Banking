package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.AppUser;
import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
    public static UserProfileResponse from(AppUser user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt()
        );
    }
}
