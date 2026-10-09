package com.codingchallenge.pokemon_api.application.service;

import com.codingchallenge.pokemon_api.application.dto.PageResponseDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonDetailDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonListDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonUpdateDto;

public interface PokemonService {
    PageResponseDto<PokemonListDto> listPokemon(int page, int size);
    PageResponseDto<PokemonListDto> listPokemon(int page, int size, String search);
    PokemonDetailDto getPokemonDetail(Long id);
    PokemonDetailDto getPokemonDetailByPokeApiId(int pokeApiId);
    PokemonDetailDto syncPokemon(int pokeApiId);
    PokemonDetailDto syncPokemon(String nameOrId);
    PokemonDetailDto updatePokemon(Long id, PokemonUpdateDto dto);
    void deletePokemon(Long id);
}
