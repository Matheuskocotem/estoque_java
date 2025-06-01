package com.example.estoque.infrastructure.security;

import com.example.estoque.domain.model.user.User;
import com.example.estoque.domain.model.user.Role;
import com.example.estoque.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private User testUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(MockFilterChain.class);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken_SetsAuthentication() throws Exception {
        // Arrange
        String token = "valid.token.here";
        request.addHeader("Authorization", "Bearer " + token);
        
        // Configura o usuário com um papel (role) válido
        testUser.setRole(Role.USER);
        
        // Configura o UserDetails para retornar as autoridades corretas
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername(testUser.getEmail())
            .password(testUser.getPassword())
            .roles(testUser.getRole().name())
            .build();
            
        when(jwtService.extractUsername(token)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUser.getEmail(), SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
    }

    @Test
    void doFilterInternal_WithInvalidToken_DoesNotSetAuthentication() throws Exception {
        // Arrange
        String token = "invalid.token.here";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithoutToken_DoesNotSetAuthentication() throws Exception {
        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void doFilterInternal_WithExpiredToken_DoesNotSetAuthentication() throws Exception {
        // Arrange
        String token = "expired.token.here";
        request.addHeader("Authorization", "Bearer " + token);
        
        when(jwtService.extractUsername(token)).thenReturn(testUser.getEmail());
        when(userDetailsService.loadUserByUsername(testUser.getEmail())).thenReturn(testUser);
        when(jwtService.isTokenValid(token, testUser)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
