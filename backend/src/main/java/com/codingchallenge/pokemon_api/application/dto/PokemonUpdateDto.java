package com.codingchallenge.pokemon_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PokemonUpdateDto(
        @NotBlank(message = "Name cannot be blank")
        String name,
        
        @Size(max = 100, message = "Local name cannot exceed 100 characters")
        String localName,
        
        @Size(max = 100, message = "Region cannot exceed 100 characters")
        String region,
        
        @Size(max = 100, message = "Custom tag cannot exceed 100 characters")
        String customTag
) {}
