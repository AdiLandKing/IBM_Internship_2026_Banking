package com.elsys.safebanking.validation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public final class EPinPolicy {

    public static final int LENGTH = 6;
    public static final int ENCRYPTED_COLUMN_LENGTH = 255;
    public static final String VALIDATION_MESSAGE = "E-PIN must contain exactly 6 digits";

    private static final int EXCLUSIVE_UPPER_BOUND = 1_000_000;
    private static final Pattern DIGITS = Pattern.compile("\\d{" + LENGTH + "}");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EPinPolicy() {
    }

    public static boolean isValid(String ePin) {
        return ePin != null && DIGITS.matcher(ePin).matches();
    }

    public static String resolveOrGenerate(String requestedEPin) {
        return requestedEPin == null || requestedEPin.isEmpty()
                ? generate()
                : requestedEPin;
    }

    public static String generate() {
        return ("%0" + LENGTH + "d").formatted(SECURE_RANDOM.nextInt(EXCLUSIVE_UPPER_BOUND));
    }

    public static boolean matches(String expected, String supplied) {
        if (expected == null || supplied == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8)
        );
    }
}
