package com.codingchallenge.pokemon_api.application.service;

import com.codingchallenge.pokemon_api.application.dto.AuthRequestDto;
import com.codingchallenge.pokemon_api.application.dto.AuthResponseDto;
import com.codingchallenge.pokemon_api.application.dto.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto register(RegisterRequestDto dto);
    AuthResponseDto login(AuthRequestDto dto);
}
