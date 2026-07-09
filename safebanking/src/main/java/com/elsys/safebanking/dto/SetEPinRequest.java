package com.elsys.safebanking.dto;

import com.elsys.safebanking.validation.ValidEPin;
import jakarta.validation.constraints.NotBlank;

public record SetEPinRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New E-PIN is required")
        @ValidEPin
        String newEPin
) {
}
