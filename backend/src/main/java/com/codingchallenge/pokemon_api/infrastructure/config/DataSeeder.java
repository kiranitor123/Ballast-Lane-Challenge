package com.codingchallenge.pokemon_api.infrastructure.config;

import com.codingchallenge.pokemon_api.application.service.PokemonService;
import com.codingchallenge.pokemon_api.domain.model.Role;
import com.codingchallenge.pokemon_api.domain.model.User;
import com.codingchallenge.pokemon_api.domain.repository.PokemonRepository;
import com.codingchallenge.pokemon_api.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PokemonRepository pokemonRepository;
    private final PokemonService pokemonService;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      PokemonRepository pokemonRepository,
                      PokemonService pokemonService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.pokemonRepository = pokemonRepository;
        this.pokemonService = pokemonService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data seeder...");

        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating default admin user...");
            User admin = User.builder()
                    .username("admin")
                    .email("admin@pokemon.api")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created (admin / admin123).");
        } else {
            log.info("Admin user already exists.");
        }

        if (pokemonRepository.count() == 0) {
            log.info("No Pokémon found in database. Syncing the first 20 Pokémon from PokeAPI...");
            for (int i = 1; i <= 20; i++) {
                try {
                    pokemonService.syncPokemon(i);
                    log.info("Successfully synced Pokémon with ID: {}", i);
                } catch (Exception e) {
                    log.error("Failed to sync Pokémon with ID: {}", i, e);
                }
            }
            log.info("Pokémon sync completed.");
        } else {
            log.info("Pokémon data already exists. Skipping sync.");
        }

        log.info("Data seeder finished.");
    }
}
