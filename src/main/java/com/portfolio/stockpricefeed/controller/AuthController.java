package com.portfolio.stockpricefeed.controller;

import com.portfolio.stockpricefeed.dto.request.RegisterRequest;
import com.portfolio.stockpricefeed.dto.response.ApiResponse;
import com.portfolio.stockpricefeed.dto.response.RegisterResponse;
import com.portfolio.stockpricefeed.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * US1 - POST /api/auth/register
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * US1 - Register new user.
     *
     * POST /api/auth/register
     * Body: { "username": "john", "email": "john@example.com", "password": "Pass@123" }
     *
     * Success  → 201 CREATED  + RegisterResponse
     * Conflict → 409 CONFLICT + error message
     * Invalid  → 400 BAD REQUEST + validation errors
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("[API] POST /api/auth/register → username={}", request.getUsername());
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

}