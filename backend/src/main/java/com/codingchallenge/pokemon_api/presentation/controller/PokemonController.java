package com.codingchallenge.pokemon_api.presentation.controller;

import com.codingchallenge.pokemon_api.application.dto.PageResponseDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonDetailDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonListDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonSyncRequestDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonUpdateDto;
import com.codingchallenge.pokemon_api.application.service.PokemonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping
    public PageResponseDto<PokemonListDto> listPokemon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return pokemonService.listPokemon(page, size, search);
    }

    @GetMapping("/{id}")
    public PokemonDetailDto getPokemonDetail(@PathVariable Long id) {
        return pokemonService.getPokemonDetail(id);
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.CREATED)
    public PokemonDetailDto syncPokemon(@Valid @RequestBody PokemonSyncRequestDto request) {
        return pokemonService.syncPokemon(request.pokeApiId());
    }

    @PostMapping("/sync/{identifier}")
    @ResponseStatus(HttpStatus.CREATED)
    public PokemonDetailDto syncPokemonByIdentifier(@PathVariable String identifier) {
        try {
            int id = Integer.parseInt(identifier.trim());
            return pokemonService.syncPokemon(id);
        } catch (NumberFormatException e) {
            return pokemonService.syncPokemon(identifier.trim());
        }
    }

    @PutMapping("/{id}")
    public PokemonDetailDto updatePokemon(
            @PathVariable Long id,
            @Valid @RequestBody PokemonUpdateDto request) {
        return pokemonService.updatePokemon(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePokemon(@PathVariable Long id) {
        pokemonService.deletePokemon(id);
    }
}
