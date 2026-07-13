package com.elsys.safebanking.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateBankAccountRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validRequestProducesNoViolations() {
        CreateBankAccountRequest request = new CreateBankAccountRequest("Main Account", "BGN");

        assertThat(validate(request)).isEmpty();
    }

    @Test
    void blankNameProducesViolation() {
        CreateBankAccountRequest request = new CreateBankAccountRequest("   ", "EUR");

        assertThat(validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name"));
    }

    @Test
    void longNameProducesViolation() {
        CreateBankAccountRequest request = new CreateBankAccountRequest("A".repeat(81), "EUR");

        assertThat(validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("name"));
    }

    @Test
    void unsupportedCurrencyProducesViolation() {
        CreateBankAccountRequest request = new CreateBankAccountRequest("Main Account", "JPY");

        assertThat(validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("currency"));
    }

    @Test
    void supportedCurrenciesProduceNoViolations() {
        for (String currency : new String[]{"BGN", "EUR", "USD", "GBP"}) {
            assertThat(validate(new CreateBankAccountRequest("Main Account", currency))).isEmpty();
        }
    }

    private Set<ConstraintViolation<CreateBankAccountRequest>> validate(CreateBankAccountRequest request) {
        return validator.validate(request);
    }
}
