package com.elsys.safebanking.dto;

import com.elsys.safebanking.model.User;
import java.time.Instant;
import java.time.LocalDate;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String role,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
