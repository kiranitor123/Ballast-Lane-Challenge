package com.codingchallenge.pokemon_api.application.dto;

import java.util.List;

public record PokemonListDto(
        Long id,
        Integer pokeApiId,
        String name,
        String spriteUrl,
        Integer weight,
        List<String> types,
        List<String> abilities
) {}
