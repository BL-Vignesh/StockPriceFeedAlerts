package com.portfolio.stockpricefeed.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

/**
 * US1 - Password validation using Java 8 Predicate (as required).
 *
 * Rules:
 *  - Minimum 8 characters
 *  - At least one uppercase letter
 *  - At least one lowercase letter
 *  - At least one digit
 *  - At least one special character from [@, #, $, %, ^, *, -, _]
 */
@Slf4j
public class PasswordValidator {

    private static final Predicate<String> hasMinLength =
            pwd -> pwd != null && pwd.length() >= 8;

    private static final Predicate<String> hasUpperCase =
            pwd -> pwd != null && pwd.chars().anyMatch(Character::isUpperCase);

    private static final Predicate<String> hasLowerCase =
            pwd -> pwd != null && pwd.chars().anyMatch(Character::isLowerCase);

    private static final Predicate<String> hasDigit =
            pwd -> pwd != null && pwd.chars().anyMatch(Character::isDigit);

    private static final Predicate<String> hasSpecialChar =
            pwd -> pwd != null && pwd.chars()
                    .mapToObj(c -> (char) c)
                    .anyMatch(c -> "@#$%^*-_".indexOf(c) >= 0);

    // Combined predicate — all rules must pass
    private static final Predicate<String> isValidPassword =
            hasMinLength
                    .and(hasUpperCase)
                    .and(hasLowerCase)
                    .and(hasDigit)
                    .and(hasSpecialChar);

    public static boolean isValid(String password) {
        return isValidPassword.test(password);
    }

    /**
     * Returns a human-readable failure reason for logging/debugging.
     * Not exposed to the client (security best practice).
     */
    public static String getFailureReason(String password) {
        if (!hasMinLength.test(password))    return "Password must be at least 8 characters";
        if (!hasUpperCase.test(password))    return "Password must contain at least one uppercase letter";
        if (!hasLowerCase.test(password))    return "Password must contain at least one lowercase letter";
        if (!hasDigit.test(password))        return "Password must contain at least one digit";
        if (!hasSpecialChar.test(password))  return "Password must contain at least one special character (@#$%^*-_)";
        return "Valid";
    }
}