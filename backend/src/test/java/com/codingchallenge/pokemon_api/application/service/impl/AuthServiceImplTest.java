package com.codingchallenge.pokemon_api.application.service.impl;

import com.codingchallenge.pokemon_api.application.dto.AuthRequestDto;
import com.codingchallenge.pokemon_api.application.dto.AuthResponseDto;
import com.codingchallenge.pokemon_api.application.dto.RegisterRequestDto;
import com.codingchallenge.pokemon_api.domain.model.Role;
import com.codingchallenge.pokemon_api.domain.model.User;
import com.codingchallenge.pokemon_api.domain.repository.UserRepository;
import com.codingchallenge.pokemon_api.infrastructure.security.JwtService;
import com.codingchallenge.pokemon_api.presentation.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.USER)
                .build();
    }

    @Test
    void testRegister_Success() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@example.com", "password");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("mock_token");

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock_token", response.token());
        assertEquals("testuser", response.username());
        assertEquals(Role.USER, response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@example.com", "password");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@example.com", "password");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void testLogin_Success() {
        AuthRequestDto request = new AuthRequestDto("testuser", "password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn("mock_token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock_token", response.token());
        assertEquals("testuser", response.username());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLogin_InvalidCredentials_ThrowsException() {
        AuthRequestDto request = new AuthRequestDto("testuser", "wrong_password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }
}
