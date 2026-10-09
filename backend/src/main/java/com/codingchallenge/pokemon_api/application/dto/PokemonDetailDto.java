package com.codingchallenge.pokemon_api.application.dto;

import java.util.List;
import java.util.Map;

public record PokemonDetailDto(
        Long id,
        Integer pokeApiId,
        String name,
        String spriteUrl,
        Integer weight,
        Integer height,
        List<String> types,
        List<String> abilities,
        Map<String, Integer> stats,
        String description,
        List<EvolutionStageDto> evolutionChain,
        String localName,
        String region,
        String customTag
) {}
