package com.portfolio.stockpricefeed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * US2 - Login request payload.
 * User can login with either email or username.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    private String emailOrUsername;

    @NotBlank(message = "Password is required")
    private String password;
}