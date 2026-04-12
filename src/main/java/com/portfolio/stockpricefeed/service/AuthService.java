package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.request.RegisterRequest;
import com.portfolio.stockpricefeed.dto.response.RegisterResponse;
import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.exception.UserAlreadyExistsException;
import com.portfolio.stockpricefeed.repository.UserRepository;
import com.portfolio.stockpricefeed.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * US1 - User Registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── US1: Register ─────────────────────────────────────────────

    /**
     * Registers a new user.
     *
     * Steps:
     * 1. Check duplicate email
     * 2. Check duplicate username
     * 3. Validate password via Java 8 Predicate (PasswordValidator)
     * 4. Encrypt password with BCrypt
     * 5. Save to PostgreSQL
     *
     * Console output:
     * [REGISTER] Attempt → username=john email=john@example.com
     * [REGISTER] Success → userId=1 username=john
     */
    public RegisterResponse register(RegisterRequest request) {
        log.info("[REGISTER] Attempt → username={} email={}", request.getUsername(), request.getEmail());

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[REGISTER] Failed → Email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Check duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("[REGISTER] Failed → Username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        // Validate password using Java 8 Predicate
        if (!PasswordValidator.isValid(request.getPassword())) {
            String reason = PasswordValidator.getFailureReason(request.getPassword());
            log.warn("[REGISTER] Failed → Password validation: {}", reason);
            throw new IllegalArgumentException(reason);
        }

        // Encrypt password using BCrypt
        String encryptedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("[REGISTER] Password encrypted successfully for user={}", request.getUsername());

        // Build and save user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encryptedPassword)
                .build();

        User savedUser = userRepository.save(user);
        log.info("[REGISTER] Success → userId={} username={} email={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("Registration successful! Please login.")
                .build();
    }

}