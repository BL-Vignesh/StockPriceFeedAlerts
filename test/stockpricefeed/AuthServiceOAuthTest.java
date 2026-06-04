package stockpricefeed;

import com.portfolio.stockpricefeed.dto.request.LoginRequest;
import com.portfolio.stockpricefeed.dto.request.RegisterRequest;
import com.portfolio.stockpricefeed.dto.response.LoginResponse;
import com.portfolio.stockpricefeed.dto.response.RegisterResponse;
import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.exception.InvalidCredentialsException;
import com.portfolio.stockpricefeed.exception.UserAlreadyExistsException;
import com.portfolio.stockpricefeed.repository.UserRepository;
import com.portfolio.stockpricefeed.security.JwtUtil;
import com.portfolio.stockpricefeed.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceOAuthTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // Use Mockito Spy to override fetchGoogleTokenInfo HTTP call in unit tests
    @InjectMocks
    @Spy
    private AuthService authService;

    private Map<String, Object> validGooglePayload;

    @BeforeEach
    void setUp() {
        validGooglePayload = new HashMap<>();
        validGooglePayload.put("iss", "https://accounts.google.com");
        validGooglePayload.put("email", "john@gmail.com");
        validGooglePayload.put("email_verified", true);
        validGooglePayload.put("exp", String.valueOf((System.currentTimeMillis() / 1000) + 3600)); // 1 hour in future
    }

    @Test
    void testRegisterGoogleUser_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@gmail.com");
        request.setPassword("Google@123");
        request.setGoogleIdToken("mockValidGoogleToken");

        // Stub the HTTP verification call
        doReturn(validGooglePayload).when(authService).fetchGoogleTokenInfo("mockValidGoogleToken");
        
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("Google@123")).thenReturn("encryptedPassword");
        
        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setUsername("john");
        savedUser.setEmail("john@gmail.com");
        savedUser.setPassword("encryptedPassword");
        savedUser.setGoogleUser(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getEmail()).isEqualTo("john@gmail.com");
        assertThat(response.getMessage()).contains("successful");
        
        verify(authService).fetchGoogleTokenInfo("mockValidGoogleToken");
        verify(userRepository).save(argThat(User::isGoogleUser));
    }

    @Test
    void testRegisterGoogleUser_AlreadyExists_ReturnsUpsertSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@gmail.com");
        request.setPassword("Google@123");
        request.setGoogleIdToken("mockValidGoogleToken");

        doReturn(validGooglePayload).when(authService).fetchGoogleTokenInfo("mockValidGoogleToken");
        when(userRepository.existsByEmail("john@gmail.com")).thenReturn(true);
        
        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("john");
        existingUser.setEmail("john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(existingUser));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getMessage()).contains("already registered via Google");
        
        // Should not try to save user or check username conflicts
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    void testLoginGoogleUser_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("john@gmail.com");
        request.setPassword("Google@123");
        request.setGoogleIdToken("mockValidGoogleToken");

        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("john");
        existingUser.setEmail("john@gmail.com");
        existingUser.setGoogleUser(true);

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(existingUser));
        doReturn(validGooglePayload).when(authService).fetchGoogleTokenInfo("mockValidGoogleToken");
        when(jwtUtil.generateToken(10L, "john")).thenReturn("mockAppJwtToken");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mockAppJwtToken");
        assertThat(response.getUsername()).isEqualTo("john");
        
        // Password matches check should be bypassed
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLoginGoogleUser_InvalidToken_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("john@gmail.com");
        request.setPassword("Google@123");
        request.setGoogleIdToken("mockInvalidToken");

        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("john");
        existingUser.setEmail("john@gmail.com");
        existingUser.setGoogleUser(true);

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(existingUser));
        
        Map<String, Object> errorPayload = new HashMap<>();
        errorPayload.put("error", "invalid_token");
        doReturn(errorPayload).when(authService).fetchGoogleTokenInfo("mockInvalidToken");

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid Google ID token");
    }

    @Test
    void testLoginStandardUser_AttemptsGoogleAccount_Blocked() {
        // Arrange: Try to log in with password 'Google@123' on a Google account without token
        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("john@gmail.com");
        request.setPassword("Google@123");
        request.setGoogleIdToken(null); // No token sent

        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("john");
        existingUser.setEmail("john@gmail.com");
        existingUser.setGoogleUser(true); // Account created via Google OAuth

        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Please sign in using Google");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
