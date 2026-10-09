package com.codingchallenge.pokemon_api.application.dto;

public record EvolutionStageDto(
        String name,
        Integer pokeApiId,
        String spriteUrl,
        Integer minLevel
) {}
