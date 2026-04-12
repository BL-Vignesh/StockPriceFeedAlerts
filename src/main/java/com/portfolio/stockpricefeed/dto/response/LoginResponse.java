package com.portfolio.stockpricefeed.dto.response;

import lombok.*;

/** US2 - Login success response with JWT token. */
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String email;
}