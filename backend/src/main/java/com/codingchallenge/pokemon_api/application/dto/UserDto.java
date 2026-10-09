package com.codingchallenge.pokemon_api.application.dto;

import com.codingchallenge.pokemon_api.domain.model.Role;
import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {}
