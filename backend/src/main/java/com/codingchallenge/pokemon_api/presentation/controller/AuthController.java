package com.codingchallenge.pokemon_api.presentation.controller;

import com.codingchallenge.pokemon_api.application.dto.AuthRequestDto;
import com.codingchallenge.pokemon_api.application.dto.AuthResponseDto;
import com.codingchallenge.pokemon_api.application.dto.RegisterRequestDto;
import com.codingchallenge.pokemon_api.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody AuthRequestDto request) {
        return authService.login(request);
    }
}
