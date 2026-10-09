package com.codingchallenge.pokemon_api.presentation.controller;

import tools.jackson.databind.ObjectMapper;
import com.codingchallenge.pokemon_api.application.dto.AuthRequestDto;
import com.codingchallenge.pokemon_api.application.dto.AuthResponseDto;
import com.codingchallenge.pokemon_api.application.dto.RegisterRequestDto;
import com.codingchallenge.pokemon_api.application.service.AuthService;
import com.codingchallenge.pokemon_api.domain.model.Role;
import com.codingchallenge.pokemon_api.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister_ValidInput_Returns201() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto("testuser", "test@example.com", "password123");
        AuthResponseDto response = new AuthResponseDto("mock_token", "testuser", Role.USER);

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock_token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testRegister_InvalidInput_Returns400() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto("", "invalid_email", "12");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testLogin_ValidCredentials_ReturnsOk() throws Exception {
        AuthRequestDto request = new AuthRequestDto("testuser", "password123");
        AuthResponseDto response = new AuthResponseDto("mock_token", "testuser", Role.USER);

        when(authService.login(any(AuthRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock_token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
