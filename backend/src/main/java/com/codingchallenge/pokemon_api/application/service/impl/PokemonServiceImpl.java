package com.codingchallenge.pokemon_api.application.service.impl;

import tools.jackson.databind.JsonNode;
import com.codingchallenge.pokemon_api.application.dto.*;
import com.codingchallenge.pokemon_api.application.service.PokemonService;
import com.codingchallenge.pokemon_api.domain.model.Pokemon;
import com.codingchallenge.pokemon_api.domain.model.PokemonAbility;
import com.codingchallenge.pokemon_api.domain.repository.PokemonRepository;
import com.codingchallenge.pokemon_api.infrastructure.external.PokeApiClient;
import com.codingchallenge.pokemon_api.presentation.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PokemonServiceImpl implements PokemonService {

    private final PokemonRepository pokemonRepository;
    private final PokeApiClient pokeApiClient;

    public PokemonServiceImpl(PokemonRepository pokemonRepository, PokeApiClient pokeApiClient) {
        this.pokemonRepository = pokemonRepository;
        this.pokeApiClient = pokeApiClient;
    }

    @Override
    public PageResponseDto<PokemonListDto> listPokemon(int page, int size) {
        return listPokemon(page, size, null);
    }

    @Override
    @Cacheable(value = "pokemon-list", key = "#page + '-' + #size + '-' + (#search != null ? #search : '')")
    public PageResponseDto<PokemonListDto> listPokemon(int page, int size, String search) {
        Page<Pokemon> pokemonPage;
        if (search != null && !search.trim().isEmpty()) {
            String query = search.trim();
            pokemonPage = pokemonRepository.findByNameContainingIgnoreCase(query, PageRequest.of(page, size, Sort.by("pokeApiId").ascending()));
            
            // Fallback to PokeAPI search on local cache miss.
            if (pokemonPage.isEmpty()) {
                boolean synced = false;
                try {
                    syncPokemon(query);
                    synced = true;
                } catch (Exception e) {
                    try {
                        JsonNode listNode = pokeApiClient.getPokemonList(0, 1000);
                        if (listNode != null && listNode.has("results")) {
                            String lowerQuery = query.toLowerCase();
                            for (JsonNode item : listNode.get("results")) {
                                String candidateName = item.get("name").asString();
                                if (candidateName.contains(lowerQuery)) {
                                    try {
                                        syncPokemon(candidateName);
                                        synced = true;
                                        break;
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (synced) {
                    pokemonPage = pokemonRepository.findByNameContainingIgnoreCase(query, PageRequest.of(page, size, Sort.by("pokeApiId").ascending()));
                }
            }
        } else {
            pokemonPage = pokemonRepository.findAll(PageRequest.of(page, size, Sort.by("pokeApiId").ascending()));
            
            // Fetch missing page batch from PokeAPI if not cached locally.
            int offset = page * size;
            if (pokemonPage.isEmpty() || pokemonPage.getContent().size() < size) {
                try {
                    JsonNode listNode = pokeApiClient.getPokemonList(offset, size);
                    if (listNode != null && listNode.has("results") && listNode.get("results").isArray()) {
                        JsonNode results = listNode.get("results");
                        if (results.size() > 0) {
                            List<CompletableFuture<JsonNode>> futures = new ArrayList<>();
                            for (JsonNode item : results) {
                                String pName = item.get("name").asString();
                                futures.add(CompletableFuture.supplyAsync(() -> {
                                    try {
                                        return pokeApiClient.getPokemon(pName);
                                    } catch (Exception e) {
                                        return null;
                                    }
                                }));
                            }
                            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                            
                            for (CompletableFuture<JsonNode> future : futures) {
                                JsonNode pokeData = future.join();
                                if (pokeData != null) {
                                    try {
                                        persistPokemonFromPokeData(pokeData);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            
                            pokemonPage = pokemonRepository.findAll(PageRequest.of(page, size, Sort.by("pokeApiId").ascending()));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        
        long totalElements = pokemonPage.getTotalElements();
        int totalPages = pokemonPage.getTotalPages();
        if (search == null || search.trim().isEmpty()) {
            try {
                JsonNode listInfo = pokeApiClient.getPokemonList(0, 1);
                if (listInfo != null && listInfo.has("count")) {
                    long apiCount = listInfo.get("count").asLong();
                    totalElements = Math.max(totalElements, apiCount);
                    totalPages = (int) Math.ceil((double) totalElements / size);
                }
            } catch (Exception ignored) {
            }
        }

        List<PokemonListDto> content = pokemonPage.getContent().stream()
                .map(this::mapToListDto)
                .collect(Collectors.toList());

        return new PageResponseDto<>(
                content,
                pokemonPage.getNumber(),
                pokemonPage.getSize(),
                totalElements,
                totalPages
        );
    }

    @Override
    @Cacheable(value = "pokemon-detail", key = "#id")
    public PokemonDetailDto getPokemonDetail(Long id) {
        Pokemon pokemon = findOrSyncPokemon(id);
        return buildPokemonDetailDto(pokemon);
    }

    @Override
    public PokemonDetailDto getPokemonDetailByPokeApiId(int pokeApiId) {
        Pokemon pokemon = pokemonRepository.findByPokeApiId(pokeApiId)
                .orElseGet(() -> {
                    try {
                        syncPokemon(pokeApiId);
                        return pokemonRepository.findByPokeApiId(pokeApiId).orElse(null);
                    } catch (Exception e) {
                        return null;
                    }
                });
        if (pokemon == null) {
            throw new ResourceNotFoundException("Pokemon not found with PokeAPI id: " + pokeApiId);
        }
        return buildPokemonDetailDto(pokemon);
    }

    private Pokemon findOrSyncPokemon(Long id) {
        Optional<Pokemon> pokemonOpt = pokemonRepository.findById(id);
        if (pokemonOpt.isPresent()) {
            return pokemonOpt.get();
        }

        if (id <= Integer.MAX_VALUE) {
            Optional<Pokemon> byPokeApiId = pokemonRepository.findByPokeApiId(id.intValue());
            if (byPokeApiId.isPresent()) {
                return byPokeApiId.get();
            }

            try {
                syncPokemon(id.intValue());
                Optional<Pokemon> synced = pokemonRepository.findByPokeApiId(id.intValue());
                if (synced.isPresent()) {
                    return synced.get();
                }
            } catch (Exception ignored) {
            }
        }

        throw new ResourceNotFoundException("Pokemon not found with id: " + id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"pokemon-list", "pokemon-detail"}, allEntries = true)
    public PokemonDetailDto syncPokemon(int pokeApiId) {
        JsonNode pokeData = pokeApiClient.getPokemon(pokeApiId);
        return persistPokemonFromPokeData(pokeData);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"pokemon-list", "pokemon-detail"}, allEntries = true)
    public PokemonDetailDto syncPokemon(String nameOrId) {
        JsonNode pokeData;
        try {
            int id = Integer.parseInt(nameOrId.trim());
            pokeData = pokeApiClient.getPokemon(id);
        } catch (NumberFormatException e) {
            pokeData = pokeApiClient.getPokemon(nameOrId.trim());
        }
        return persistPokemonFromPokeData(pokeData);
    }

    private PokemonDetailDto persistPokemonFromPokeData(JsonNode pokeData) {
        int pokeApiId = pokeData.get("id").asInt();
        Pokemon pokemon = pokemonRepository.findByPokeApiId(pokeApiId).orElse(new Pokemon());
        
        pokemon.setPokeApiId(pokeApiId);
        pokemon.setName(pokeData.get("name").asString());
        pokemon.setWeight(pokeData.get("weight").asInt());
        pokemon.setHeight(pokeData.get("height").asInt());
        
        JsonNode sprites = pokeData.get("sprites");
        if (sprites.has("other") && sprites.get("other").has("official-artwork") && !sprites.get("other").get("official-artwork").get("front_default").isNull()) {
            pokemon.setSpriteUrl(sprites.get("other").get("official-artwork").get("front_default").asString());
        } else if (!sprites.get("front_default").isNull()) {
            pokemon.setSpriteUrl(sprites.get("front_default").asString());
        }

        List<String> typesList = new ArrayList<>();
        pokeData.get("types").forEach(typeNode -> 
            typesList.add(typeNode.get("type").get("name").asString())
        );
        pokemon.setTypes(String.join(",", typesList));

        pokeData.get("stats").forEach(statNode -> {
            String statName = statNode.get("stat").get("name").asString();
            int baseStat = statNode.get("base_stat").asInt();
            switch (statName) {
                case "hp" -> pokemon.setHp(baseStat);
                case "attack" -> pokemon.setAttack(baseStat);
                case "defense" -> pokemon.setDefense(baseStat);
                case "special-attack" -> pokemon.setSpecialAttack(baseStat);
                case "special-defense" -> pokemon.setSpecialDefense(baseStat);
                case "speed" -> pokemon.setSpeed(baseStat);
            }
        });

        if (pokemon.getId() != null) {
            pokemon.clearAbilities();
        }
        
        pokeData.get("abilities").forEach(abilityNode -> {
            PokemonAbility ability = new PokemonAbility();
            ability.setName(abilityNode.get("ability").get("name").asString());
            ability.setIsHidden(abilityNode.get("is_hidden").asBoolean());
            pokemon.addAbility(ability);
        });

        pokemon.setSynced(true);
        pokemon.setSyncedAt(LocalDateTime.now());

        pokemonRepository.save(pokemon);
        return buildPokemonDetailDto(pokemon);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"pokemon-list", "pokemon-detail"}, allEntries = true)
    public PokemonDetailDto updatePokemon(Long id, PokemonUpdateDto dto) {
        Pokemon pokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pokemon not found with id: " + id));

        pokemon.setName(dto.name());
        pokemon.setLocalName(dto.localName());
        pokemon.setRegion(dto.region());
        pokemon.setCustomTag(dto.customTag());

        pokemonRepository.save(pokemon);
        return buildPokemonDetailDto(pokemon);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"pokemon-list", "pokemon-detail"}, allEntries = true)
    public void deletePokemon(Long id) {
        if (!pokemonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pokemon not found with id: " + id);
        }
        pokemonRepository.deleteById(id);
    }

    private PokemonListDto mapToListDto(Pokemon pokemon) {
        return new PokemonListDto(
                pokemon.getId(),
                pokemon.getPokeApiId(),
                pokemon.getName(),
                pokemon.getSpriteUrl(),
                pokemon.getWeight(),
                pokemon.getTypes() != null ? Arrays.asList(pokemon.getTypes().split(",")) : Collections.emptyList(),
                pokemon.getAbilities().stream().map(PokemonAbility::getName).collect(Collectors.toList())
        );
    }

    private PokemonDetailDto buildPokemonDetailDto(Pokemon pokemon) {
        String description = "No description available";
        List<EvolutionStageDto> evolutionChain = new ArrayList<>();
        
        try {
            JsonNode speciesData = pokeApiClient.getPokemonSpecies(pokemon.getPokeApiId());
            
            JsonNode flavorTextEntries = speciesData.get("flavor_text_entries");
            if (flavorTextEntries != null && flavorTextEntries.isArray()) {
                for (JsonNode entry : flavorTextEntries) {
                    if (entry.get("language").get("name").asString().equals("en")) {
                        description = entry.get("flavor_text").asString().replace("\n", " ").replace("\f", " ");
                        break;
                    }
                }
            }

            JsonNode evoChainUrlNode = speciesData.get("evolution_chain");
            if (evoChainUrlNode != null && !evoChainUrlNode.isNull() && evoChainUrlNode.has("url")) {
                JsonNode evoData = pokeApiClient.getEvolutionChain(evoChainUrlNode.get("url").asString());
                if (evoData != null && evoData.has("chain")) {
                    extractEvolutions(evoData.get("chain"), evolutionChain);
                }
            }
        } catch (Exception e) {
            // Non-critical metadata: return basic details if upstream fails
        }

        Map<String, Integer> stats = new HashMap<>();
        stats.put("hp", pokemon.getHp());
        stats.put("attack", pokemon.getAttack());
        stats.put("defense", pokemon.getDefense());
        stats.put("specialAttack", pokemon.getSpecialAttack());
        stats.put("specialDefense", pokemon.getSpecialDefense());
        stats.put("speed", pokemon.getSpeed());

        return new PokemonDetailDto(
                pokemon.getId(),
                pokemon.getPokeApiId(),
                pokemon.getName(),
                pokemon.getSpriteUrl(),
                pokemon.getWeight(),
                pokemon.getHeight(),
                pokemon.getTypes() != null ? Arrays.asList(pokemon.getTypes().split(",")) : Collections.emptyList(),
                pokemon.getAbilities().stream().map(PokemonAbility::getName).collect(Collectors.toList()),
                stats,
                description,
                evolutionChain,
                pokemon.getLocalName(),
                pokemon.getRegion(),
                pokemon.getCustomTag()
        );
    }

    private void extractEvolutions(JsonNode chainNode, List<EvolutionStageDto> stages) {
        String name = chainNode.get("species").get("name").asString();
        String url = chainNode.get("species").get("url").asString();
        String[] urlParts = url.split("/");
        int pokeApiId = Integer.parseInt(urlParts[urlParts.length - 1]);
        
        Integer minLevel = null;
        JsonNode detailsNode = chainNode.get("evolution_details");
        if (detailsNode != null && detailsNode.isArray() && detailsNode.size() > 0) {
            JsonNode firstDetail = detailsNode.get(0);
            if (firstDetail.has("min_level") && !firstDetail.get("min_level").isNull()) {
                minLevel = firstDetail.get("min_level").asInt();
            }
        }

        stages.add(new EvolutionStageDto(
                name,
                pokeApiId,
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + pokeApiId + ".png",
                minLevel
        ));

        JsonNode evolvesTo = chainNode.get("evolves_to");
        if (evolvesTo != null && evolvesTo.isArray()) {
            for (JsonNode nextEvo : evolvesTo) {
                extractEvolutions(nextEvo, stages);
            }
        }
    }
}
