package com.portfolio.stockpricefeed.service;

import com.portfolio.stockpricefeed.dto.request.LoginRequest;
import com.portfolio.stockpricefeed.dto.request.RegisterRequest;
import com.portfolio.stockpricefeed.dto.response.LoginResponse;
import com.portfolio.stockpricefeed.dto.response.RegisterResponse;
import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.exception.InvalidCredentialsException;
import com.portfolio.stockpricefeed.exception.UserAlreadyExistsException;
import com.portfolio.stockpricefeed.repository.UserRepository;
import com.portfolio.stockpricefeed.security.JwtUtil;
import com.portfolio.stockpricefeed.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * US1 - User Registration
 * US2 - User Login with JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

    // ── US2: Login ────────────────────────────────────────────────

    /**
     * Authenticates user and returns a JWT token.
     *
     * Steps:
     * 1. Find user by email or username (using Optional)
     * 2. Verify password using BCrypt
     * 3. Generate JWT token
     *
     * Console output:
     * [LOGIN] Attempt → emailOrUsername=john@example.com
     * [LOGIN] Success → userId=1 username=john token=eyJ...
     */
    public LoginResponse login(LoginRequest request) {
        log.info("[LOGIN] Attempt → emailOrUsername={}", request.getEmailOrUsername());

        // US2: Using Optional for user fetching (as required)
        Optional<User> userOpt = userRepository.findByEmail(request.getEmailOrUsername())
                .or(() -> userRepository.findByUsername(request.getEmailOrUsername()));

        User user = userOpt.orElseThrow(() -> {
            log.warn("[LOGIN] Failed → User not found: {}", request.getEmailOrUsername());
            return new InvalidCredentialsException("Invalid email/username or password");
        });

        // Verify password with BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[LOGIN] Failed → Incorrect password for user: {}", user.getUsername());
            throw new InvalidCredentialsException("Invalid email/username or password");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("[LOGIN] Success → userId={} username={}", user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
    // ── Profile and Password Management ───────────────────────────

    public com.portfolio.stockpricefeed.dto.response.ProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        return com.portfolio.stockpricefeed.dto.response.ProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .build();
    }

    public com.portfolio.stockpricefeed.dto.response.ProfileResponse updateProfile(String username, com.portfolio.stockpricefeed.dto.request.UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setDisplayName(request.getDisplayName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
             if (userRepository.existsByEmail(request.getEmail())) {
                 throw new UserAlreadyExistsException("Email already taken: " + request.getEmail());
             }
             user.setEmail(request.getEmail());
        }

        User savedUser = userRepository.save(user);
        
        return com.portfolio.stockpricefeed.dto.response.ProfileResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .displayName(savedUser.getDisplayName())
                .phone(savedUser.getPhone())
                .address(savedUser.getAddress())
                .build();
    }

    public LoginResponse changePassword(String username, com.portfolio.stockpricefeed.dto.request.ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        if (!PasswordValidator.isValid(request.getNewPassword())) {
            throw new IllegalArgumentException(PasswordValidator.getFailureReason(request.getNewPassword()));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}