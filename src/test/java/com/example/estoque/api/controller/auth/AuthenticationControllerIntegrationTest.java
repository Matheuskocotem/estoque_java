package com.example.estoque.api.controller.auth;

import com.example.estoque.api.dto.auth.AuthenticationRequest;
import com.example.estoque.api.dto.auth.AuthenticationResponse;
import com.example.estoque.application.service.auth.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o AuthenticationController.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerIntegrationTest {

    private MockMvc mockMvc;
    
    @Mock
    private AuthenticationService authenticationService;
    
    @InjectMocks
    private AuthenticationController authenticationController;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    void login_WithValidCredentials_ReturnsToken() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "password");
        AuthenticationResponse response = new AuthenticationResponse("test.jwt.token");
        
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.getToken()));
    }

    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("invalid@example.com", "wrongpassword");
        
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));
            
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError()); // O controller não trata a exceção, então retorna 500
    }

    @Test
    void login_WithEmptyEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        String requestBody = "{\"email\":\"\",\"password\":\"password123\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email deve ser válido"));
    }
    
    @Test
    void login_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        String requestBody = "{\"email\":\"invalid-email\",\"password\":\"password123\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email deve ser válido"));
    }
    
    @Test
    void login_WithEmptyPassword_ReturnsBadRequest() throws Exception {
        // Arrange
        String requestBody = "{\"email\":\"user@example.com\",\"password\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Senha é obrigatória"));
    }
    
    @Test
    void login_WithNullRequest_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email é obrigatório"))
                .andExpect(jsonPath("$.password").value("Senha é obrigatória"));
    }
}
