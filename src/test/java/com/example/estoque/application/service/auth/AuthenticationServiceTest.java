package com.example.estoque.application.service.auth;

import com.example.estoque.api.dto.auth.AuthenticationRequest;
import com.example.estoque.api.dto.auth.AuthenticationResponse;
import com.example.estoque.domain.model.user.Role;
import com.example.estoque.domain.model.user.User;
import com.example.estoque.domain.repository.UserRepository;
import com.example.estoque.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private AuthenticationRequest authRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);

        authRequest = new AuthenticationRequest("test@example.com", "password");
    }

    @Test
    void authenticate_WithValidCredentials_ReturnsToken() {
        // Arrange
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("test.jwt.token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test.jwt.token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void authenticate_WithInvalidCredentials_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticate(authRequest);
        });

        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }
}
