package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.Users;
import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant createdAt
) {
    public static UserProfileResponse from(Users user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
