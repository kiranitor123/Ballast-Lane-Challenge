package com.codingchallenge.pokemon_api.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PokemonSyncRequestDto(
        @NotNull(message = "PokeAPI ID is required")
        @Min(value = 1, message = "PokeAPI ID must be greater than 0")
        Integer pokeApiId
) {}
