package com.elsys.safebanking.validation;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public final class EPinPolicy {

    public static final int LENGTH = 6;
    public static final int HASHED_COLUMN_LENGTH = 255;
    public static final String VALIDATION_MESSAGE = "E-PIN must contain exactly 6 digits";

    private static final int EXCLUSIVE_UPPER_BOUND = 1_000_000;
    private static final Pattern DIGITS = Pattern.compile("\\d{" + LENGTH + "}");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EPinPolicy() {
    }

    public static boolean isValid(String ePin) {
        return ePin != null && DIGITS.matcher(ePin).matches();
    }

    public static Resolution resolve(String requestedEPin) {
        if (requestedEPin == null || requestedEPin.isEmpty()) {
            return new Resolution(generate(), true);
        }
        return new Resolution(requestedEPin, false);
    }

    public static String generate() {
        return ("%0" + LENGTH + "d").formatted(SECURE_RANDOM.nextInt(EXCLUSIVE_UPPER_BOUND));
    }

    public record Resolution(String value, boolean generated) {
    }
}
