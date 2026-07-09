package com.elsys.safebanking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EPinValidator implements ConstraintValidator<ValidEPin, String> {

    private boolean allowBlank;

    @Override
    public void initialize(ValidEPin constraintAnnotation) {
        allowBlank = constraintAnnotation.allowBlank();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value.isEmpty()) {
            return allowBlank;
        }
        return EPinPolicy.isValid(value);
    }
}
