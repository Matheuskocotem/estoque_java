package com.example.estoque.infrastructure.security;

import com.example.estoque.domain.model.user.Role;
import com.example.estoque.domain.model.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private UserDetails userDetails;

    private JwtService jwtService;
    private User testUser;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000; // 24h
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.setSecret(SECRET_KEY);
        jwtService.setExpiration(EXPIRATION);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);

        when(userDetails.getUsername()).thenReturn(testUser.getEmail());
    }

    @Test
    void generateToken_WithUserDetails_ReturnsValidToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(testUser.getEmail(), jwtService.extractUsername(token));
    }

    @Test
    void generateToken_WithExtraClaims_ReturnsValidToken() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("custom_key", "custom_value");

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(testUser.getEmail(), jwtService.extractUsername(token));
    }

    @Test
    void isTokenValid_WithValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    
    @Test
    void isTokenValid_WithInvalidSignature_ThrowsSignatureException() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid_signature";

        // Configura o UserDetails para o teste
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Act & Assert
        assertThrows(io.jsonwebtoken.security.SignatureException.class, () -> {
            jwtService.isTokenValid(invalidToken, userDetails);
        });
    }
    
    @Test
    void isTokenValid_WithMalformedToken_ThrowsMalformedJwtException() {
        // Arrange
        String malformedToken = "malformed.token";
        
        // Configura o UserDetails para o teste
        when(userDetails.getUsername()).thenReturn("test@example.com");

        // Act & Assert
        assertThrows(io.jsonwebtoken.MalformedJwtException.class, () -> {
            jwtService.isTokenValid(malformedToken, userDetails);
        });
    }
    
    @Test
    void isTokenValid_WithExpiredToken_ThrowsExpiredJwtException() {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject(testUser.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // 1 day ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 12)) // 12 hours ago
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(expiredToken, userDetails);
        });
    }

    @Test
    void extractUsername_WithValidToken_ReturnsUsername() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals(testUser.getEmail(), username);
    }
    
    @Test
    void extractExpiration_WithValidToken_ReturnsExpirationDate() {
        // Arrange
        String token = jwtService.generateToken(userDetails);
        
        // Act
        Date expiration = jwtService.extractExpiration(token);
        
        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
}
