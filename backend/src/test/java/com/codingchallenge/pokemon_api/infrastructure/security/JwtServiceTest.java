package com.codingchallenge.pokemon_api.infrastructure.security;

import com.codingchallenge.pokemon_api.domain.model.Role;
import com.codingchallenge.pokemon_api.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        mockUser = User.builder()
                .username("testuser")
                .role(Role.USER)
                .build();
    }

    @Test
    void testGenerateToken_ReturnsValidToken() {
        String token = jwtService.generateToken(mockUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractUsername_ReturnsCorrectUsername() {
        String token = jwtService.generateToken(mockUser);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testIsTokenValid_WithValidToken_ReturnsTrue() {
        String token = jwtService.generateToken(mockUser);
        assertTrue(jwtService.isTokenValid(token, mockUser));
    }

    @Test
    void testIsTokenValid_WithDifferentUser_ReturnsFalse() {
        String token = jwtService.generateToken(mockUser);
        User otherUser = User.builder().username("otheruser").role(Role.USER).build();
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void testIsTokenValid_WithExpiredToken_ThrowsException() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -10000L);
        String token = jwtService.generateToken(mockUser);

        assertThrows(Exception.class, () -> jwtService.isTokenValid(token, mockUser));
    }
}
