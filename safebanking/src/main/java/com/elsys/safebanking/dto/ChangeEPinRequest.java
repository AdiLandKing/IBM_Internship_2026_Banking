package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeEPinRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New E-PIN is required")
        @Pattern(regexp = "\\d{6}", message = "New E-PIN must contain exactly 6 digits")
        String newEPin
) {
}
