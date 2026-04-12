package com.portfolio.stockpricefeed.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * US1 - Registration request payload.
 *
 * Password rules (from requirements):
 *  - Min 8 characters
 *  - At least one uppercase letter
 *  - At least one lowercase letter
 *  - At least one digit
 *  - At least one special character from [@, #, $, %, ^, *, -, _]
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^*\\-_])[A-Za-z\\d@#$%^*\\-_]{8,}$",
            message = "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character (@#$%^*-_)"
    )
    private String password;
}