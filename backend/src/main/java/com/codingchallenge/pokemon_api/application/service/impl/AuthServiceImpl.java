package com.codingchallenge.pokemon_api.application.service.impl;

import com.codingchallenge.pokemon_api.application.dto.AuthRequestDto;
import com.codingchallenge.pokemon_api.application.dto.AuthResponseDto;
import com.codingchallenge.pokemon_api.application.dto.RegisterRequestDto;
import com.codingchallenge.pokemon_api.application.service.AuthService;
import com.codingchallenge.pokemon_api.domain.model.Role;
import com.codingchallenge.pokemon_api.domain.model.User;
import com.codingchallenge.pokemon_api.domain.repository.UserRepository;
import com.codingchallenge.pokemon_api.infrastructure.security.JwtService;
import com.codingchallenge.pokemon_api.presentation.exception.DuplicateResourceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager,
                           UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        
        return new AuthResponseDto(token, user.getUsername(), user.getRole());
    }

    @Override
    public AuthResponseDto login(AuthRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );
        
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
                
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);
        
        return new AuthResponseDto(token, user.getUsername(), user.getRole());
    }
}
