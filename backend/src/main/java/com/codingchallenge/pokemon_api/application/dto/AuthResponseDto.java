package com.codingchallenge.pokemon_api.application.dto;

import com.codingchallenge.pokemon_api.domain.model.Role;

public record AuthResponseDto(
        String token,
        String username,
        Role role
) {}
