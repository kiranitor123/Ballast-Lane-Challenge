package com.codingchallenge.pokemon_api.domain.repository;

import com.codingchallenge.pokemon_api.domain.model.Pokemon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    Optional<Pokemon> findByPokeApiId(Integer pokeApiId);
    Page<Pokemon> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByPokeApiId(Integer pokeApiId);
}
