package com.elsys.safebanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateBankAccountRequest(

        @NotBlank
        String accountName,

        @NotBlank
        String iban,

        @NotNull
        BigDecimal balance,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency
) {
}
