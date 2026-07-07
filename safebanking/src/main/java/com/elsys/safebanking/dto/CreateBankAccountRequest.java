package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBankAccountRequest(

        @NotBlank
        String accountName,

        @NotNull
        Integer balance,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency,

        @NotNull
        Long userId
) {
}
