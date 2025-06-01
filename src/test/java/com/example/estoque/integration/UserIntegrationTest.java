package com.example.estoque.integration;

import com.example.estoque.api.dto.auth.AuthenticationRequest;
import com.example.estoque.api.dto.user.UserCreateDTO;
import com.example.estoque.api.dto.user.UserResponseDTO;
import com.example.estoque.config.BaseIntegrationTest;
import com.example.estoque.domain.model.user.Role;
import com.example.estoque.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        // Cria um usuário de teste e obtém o token
        createTestUser();
        authToken = authenticateAndGetToken("test@example.com", "password123");
    }
    
    private void createTestUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName("Test User");
        userCreateDTO.setEmail("test@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setRole(Role.USER.name());
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDTO)));
    }
    
    private String authenticateAndGetToken(String email, String password) throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest(email, password);
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
                
        return objectMapper.readTree(result.getResponse().getContentAsString())
                         .get("token").asText();
    }

    @Test
    void createUser_WithValidData_ReturnsCreated() throws Exception {
        // Arrange
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName("New Test User");
        userCreateDTO.setEmail("newtest@example.com");
        userCreateDTO.setPassword("newpassword123");
        userCreateDTO.setRole(Role.USER.name());

        // Act
        MvcResult result = mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        String responseBody = result.getResponse().getContentAsString();
        UserResponseDTO responseDTO = objectMapper.readValue(responseBody, UserResponseDTO.class);

        assertNotNull(responseDTO.getId());
        assertEquals(userCreateDTO.getName(), responseDTO.getName());
        assertEquals(userCreateDTO.getEmail(), responseDTO.getEmail());
        assertEquals(userCreateDTO.getRole(), responseDTO.getRole());
    }

    @Test
    void login_WithValidCredentials_ReturnsToken() throws Exception {
        // Arrange - Create a user first
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName("Test User");
        userCreateDTO.setEmail("test@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setRole(Role.USER.name());
        
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userCreateDTO)));

        AuthenticationRequest authRequest = new AuthenticationRequest(
                "test@example.com",
                "password123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void getUsers_WhenAuthenticated_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
