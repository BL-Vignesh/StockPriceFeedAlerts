package stockpricefeed;

import com.portfolio.stockpricefeed.controller.AuthController;
import com.portfolio.stockpricefeed.dto.request.LoginRequest;
import com.portfolio.stockpricefeed.dto.request.RegisterRequest;
import com.portfolio.stockpricefeed.dto.response.LoginResponse;
import com.portfolio.stockpricefeed.dto.response.RegisterResponse;
import com.portfolio.stockpricefeed.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerHeaderTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@gmail.com");
        registerRequest.setPassword("Google@123");

        loginRequest = new LoginRequest();
        loginRequest.setEmailOrUsername("test@gmail.com");
        loginRequest.setPassword("Google@123");
    }

    @Test
    void testRegister_WithXGoogleTokenHeader_ExtractsToken() {
        // Arrange
        RegisterResponse mockResponse = RegisterResponse.builder().message("Success").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // Act
        authController.register(registerRequest, "header-token-x", null, null);

        // Assert
        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertThat(captor.getValue().getGoogleIdToken()).isEqualTo("header-token-x");
    }

    @Test
    void testRegister_WithGoogleIdTokenHeader_ExtractsToken() {
        // Arrange
        RegisterResponse mockResponse = RegisterResponse.builder().message("Success").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // Act
        authController.register(registerRequest, null, "header-token-id", null);

        // Assert
        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertThat(captor.getValue().getGoogleIdToken()).isEqualTo("header-token-id");
    }

    @Test
    void testRegister_WithAuthorizationHeader_ExtractsToken() {
        // Arrange
        RegisterResponse mockResponse = RegisterResponse.builder().message("Success").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // Act
        authController.register(registerRequest, null, null, "Bearer bearer-token");

        // Assert
        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(authService).register(captor.capture());
        assertThat(captor.getValue().getGoogleIdToken()).isEqualTo("bearer-token");
    }

    @Test
    void testLogin_WithXGoogleTokenHeader_ExtractsToken() {
        // Arrange
        LoginResponse mockResponse = LoginResponse.builder().token("app-token").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // Act
        authController.login(loginRequest, "header-token-x", null, null);

        // Assert
        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());
        assertThat(captor.getValue().getGoogleIdToken()).isEqualTo("header-token-x");
    }

    @Test
    void testLogin_WithGoogleIdTokenHeader_ExtractsToken() {
        // Arrange
        LoginResponse mockResponse = LoginResponse.builder().token("app-token").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // Act
        authController.login(loginRequest, null, "header-token-id", null);

        // Assert
        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());
        assertThat(captor.getValue().getGoogleIdToken()).isEqualTo("header-token-id");
    }

    @Test
    void testLogin_WithAuthorizationHeader_ExtractsToken() {
        // Arrange
        LoginResponse mockResponse = LoginResponse.builder().token("app-token").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        // Act
        authController.login(loginRequest, null, null, "Bearer bearer-token");

        // Assert
        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(authService).login(captor.capture());
        assertThat(captor.getValue().getGoogleIdToken()).isEqualTo("bearer-token");
    }
}
