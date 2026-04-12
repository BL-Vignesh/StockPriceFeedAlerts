package com.portfolio.stockpricefeed.dto.response;

import lombok.*;

/**
 * US1 - Registration success response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    private Long userId;
    private String username;
    private String email;
    private String message;
}