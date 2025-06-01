package com.example.estoque.application.service.auth;

import com.example.estoque.api.dto.auth.AuthenticationRequest;
import com.example.estoque.api.dto.auth.AuthenticationResponse;
import com.example.estoque.domain.repository.UserRepository;
import com.example.estoque.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );
        
        var user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            
        var jwtToken = jwtService.generateToken(new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.getAuthorities()
        ));
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }
}
