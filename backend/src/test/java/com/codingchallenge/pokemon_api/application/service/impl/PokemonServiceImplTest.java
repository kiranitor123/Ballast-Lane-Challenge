package com.codingchallenge.pokemon_api.application.service.impl;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.codingchallenge.pokemon_api.application.dto.PageResponseDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonDetailDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonListDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonUpdateDto;
import com.codingchallenge.pokemon_api.domain.model.Pokemon;
import com.codingchallenge.pokemon_api.domain.model.PokemonAbility;
import com.codingchallenge.pokemon_api.domain.repository.PokemonRepository;
import com.codingchallenge.pokemon_api.infrastructure.external.PokeApiClient;
import com.codingchallenge.pokemon_api.presentation.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PokemonServiceImplTest {

    @Mock
    private PokemonRepository pokemonRepository;

    @Mock
    private PokeApiClient pokeApiClient;

    @InjectMocks
    private PokemonServiceImpl pokemonService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Pokemon mockPokemon;

    @BeforeEach
    void setUp() {
        mockPokemon = Pokemon.builder()
                .id(1L)
                .name("bulbasaur")
                .pokeApiId(1)
                .weight(69)
                .height(7)
                .spriteUrl("https://example.com/bulbasaur.png")
                .types("grass,poison")
                .hp(45)
                .attack(49)
                .defense(49)
                .specialAttack(65)
                .specialDefense(65)
                .speed(45)
                .abilities(new ArrayList<>())
                .build();

        PokemonAbility ability = PokemonAbility.builder()
                .id(1L)
                .name("overgrow")
                .isHidden(false)
                .pokemon(mockPokemon)
                .build();
        mockPokemon.getAbilities().add(ability);
    }

    @Test
    void testListPokemon_ReturnsPagedResults() {
        Page<Pokemon> page = new PageImpl<>(List.of(mockPokemon));
        when(pokemonRepository.findAll(any(PageRequest.class))).thenReturn(page);

        PageResponseDto<PokemonListDto> result = pokemonService.listPokemon(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("bulbasaur", result.content().get(0).name());
    }

    @Test
    void testListPokemon_WithSearch_ReturnsFilteredResults() {
        Page<Pokemon> page = new PageImpl<>(List.of(mockPokemon));
        when(pokemonRepository.findByNameContainingIgnoreCase(eq("bulb"), any(PageRequest.class))).thenReturn(page);

        PageResponseDto<PokemonListDto> result = pokemonService.listPokemon(0, 10, "bulb");

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("bulbasaur", result.content().get(0).name());
    }

    @Test
    void testListPokemon_WithSearch_WhenNotInDatabase_FallsBackToPokeApi() throws Exception {
        when(pokemonRepository.findByNameContainingIgnoreCase(eq("pikachu"), any(PageRequest.class)))
                .thenReturn(Page.empty())
                .thenReturn(new PageImpl<>(List.of(mockPokemon)));

        String pikachuJson = """
            {
              "id": 25,
              "name": "pikachu",
              "weight": 60,
              "height": 4,
              "sprites": {"front_default": "https://example.com/pika.png"},
              "types": [{"type": {"name": "electric"}}],
              "abilities": [{"ability": {"name": "static"}, "is_hidden": false}],
              "stats": [{"base_stat": 35, "stat": {"name": "hp"}}]
            }
            """;
        JsonNode node = objectMapper.readTree(pikachuJson);
        when(pokeApiClient.getPokemon("pikachu")).thenReturn(node);
        when(pokemonRepository.findByPokeApiId(25)).thenReturn(Optional.empty());
        when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PageResponseDto<PokemonListDto> result = pokemonService.listPokemon(0, 10, "pikachu");

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        verify(pokeApiClient).getPokemon("pikachu");
        verify(pokemonRepository).save(any(Pokemon.class));
    }

    @Test
    void testGetPokemonDetail_WhenExists_ReturnsDetail() throws Exception {
        when(pokemonRepository.findById(1L)).thenReturn(Optional.of(mockPokemon));

        JsonNode speciesNode = objectMapper.readTree("""
            {
                "flavor_text_entries": [
                    {"flavor_text": "A strange seed was planted on its back.", "language": {"name": "en"}}
                ],
                "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/1/"}
            }
            """);
        when(pokeApiClient.getPokemonSpecies(1)).thenReturn(speciesNode);

        JsonNode evoNode = objectMapper.readTree("""
            {
                "chain": {
                    "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"},
                    "evolves_to": []
                }
            }
            """);
        when(pokeApiClient.getEvolutionChain(anyString())).thenReturn(evoNode);

        PokemonDetailDto result = pokemonService.getPokemonDetail(1L);

        assertNotNull(result);
        assertEquals("bulbasaur", result.name());
        assertEquals("A strange seed was planted on its back.", result.description());
        assertEquals(1, result.evolutionChain().size());
    }

    @Test
    void testGetPokemonDetail_WhenNotExists_ThrowsNotFoundException() {
        when(pokemonRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pokemonService.getPokemonDetail(1L));
    }

    @Test
    void testGetPokemonDetail_WhenNotLoadedInDb_AutoSyncsFromPokeApi() throws Exception {
        when(pokemonRepository.findById(26L)).thenReturn(Optional.empty());
        when(pokemonRepository.findByPokeApiId(26)).thenReturn(Optional.empty()).thenReturn(Optional.of(mockPokemon));

        String raichuJson = """
            {
              "id": 26,
              "name": "raichu",
              "weight": 300,
              "height": 8,
              "sprites": {"front_default": "https://example.com/raichu.png"},
              "types": [{"type": {"name": "electric"}}],
              "abilities": [{"ability": {"name": "static"}, "is_hidden": false}],
              "stats": [{"base_stat": 60, "stat": {"name": "hp"}}]
            }
            """;
        JsonNode raichuNode = objectMapper.readTree(raichuJson);
        when(pokeApiClient.getPokemon(26)).thenReturn(raichuNode);
        when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(inv -> inv.getArgument(0));

        PokemonDetailDto result = pokemonService.getPokemonDetail(26L);

        assertNotNull(result);
        verify(pokeApiClient).getPokemon(26);
        verify(pokemonRepository).save(any(Pokemon.class));
    }

    @Test
    void testSyncPokemon_NewPokemon_CreatesAndReturns() throws Exception {
        String pokemonJson = """
            {
              "id": 1,
              "name": "bulbasaur",
              "weight": 69,
              "height": 7,
              "sprites": {
                "front_default": "https://example.com/front.png",
                "other": {
                  "official-artwork": {
                    "front_default": "https://example.com/artwork.png"
                  }
                }
              },
              "types": [
                {"type": {"name": "grass"}},
                {"type": {"name": "poison"}}
              ],
              "abilities": [
                {"ability": {"name": "overgrow"}, "is_hidden": false}
              ],
              "stats": [
                {"base_stat": 45, "stat": {"name": "hp"}},
                {"base_stat": 49, "stat": {"name": "attack"}},
                {"base_stat": 49, "stat": {"name": "defense"}},
                {"base_stat": 65, "stat": {"name": "special-attack"}},
                {"base_stat": 65, "stat": {"name": "special-defense"}},
                {"base_stat": 45, "stat": {"name": "speed"}}
              ]
            }
            """;
        JsonNode pokemonNode = objectMapper.readTree(pokemonJson);
        when(pokeApiClient.getPokemon(1)).thenReturn(pokemonNode);
        when(pokemonRepository.findByPokeApiId(1)).thenReturn(Optional.empty());
        when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PokemonDetailDto result = pokemonService.syncPokemon(1);

        assertNotNull(result);
        assertEquals("bulbasaur", result.name());
        verify(pokemonRepository).save(any(Pokemon.class));
    }

    @Test
    void testSyncPokemon_ExistingPokemon_UpdatesAndReturns() throws Exception {
        String pokemonJson = """
            {
              "id": 1,
              "name": "bulbasaur",
              "weight": 70,
              "height": 7,
              "sprites": {"front_default": "https://example.com/front.png"},
              "types": [{"type": {"name": "grass"}}],
              "abilities": [{"ability": {"name": "overgrow"}, "is_hidden": false}],
              "stats": [{"base_stat": 45, "stat": {"name": "hp"}}]
            }
            """;
        JsonNode pokemonNode = objectMapper.readTree(pokemonJson);
        when(pokeApiClient.getPokemon(1)).thenReturn(pokemonNode);
        when(pokemonRepository.findByPokeApiId(1)).thenReturn(Optional.of(mockPokemon));
        when(pokemonRepository.save(any(Pokemon.class))).thenReturn(mockPokemon);

        PokemonDetailDto result = pokemonService.syncPokemon(1);

        assertNotNull(result);
        verify(pokemonRepository).save(any(Pokemon.class));
    }

    @Test
    void testUpdatePokemon_WhenExists_UpdatesFields() {
        when(pokemonRepository.findById(1L)).thenReturn(Optional.of(mockPokemon));
        when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PokemonUpdateDto dto = new PokemonUpdateDto("Bulbasaur Custom", "Fushigidane", "Kanto", "Starter");
        PokemonDetailDto result = pokemonService.updatePokemon(1L, dto);

        assertNotNull(result);
        assertEquals("Bulbasaur Custom", result.name());
        assertEquals("Fushigidane", result.localName());
        assertEquals("Kanto", result.region());
        assertEquals("Starter", result.customTag());
        verify(pokemonRepository).save(any(Pokemon.class));
    }

    @Test
    void testUpdatePokemon_WhenNotExists_ThrowsNotFoundException() {
        when(pokemonRepository.findById(1L)).thenReturn(Optional.empty());

        PokemonUpdateDto dto = new PokemonUpdateDto("Bulbasaur", "Local", "Region", "Tag");
        assertThrows(ResourceNotFoundException.class, () -> pokemonService.updatePokemon(1L, dto));
    }

    @Test
    void testDeletePokemon_WhenExists_Deletes() {
        when(pokemonRepository.existsById(1L)).thenReturn(true);
        doNothing().when(pokemonRepository).deleteById(1L);

        pokemonService.deletePokemon(1L);

        verify(pokemonRepository).deleteById(1L);
    }

    @Test
    void testDeletePokemon_WhenNotExists_ThrowsNotFoundException() {
        when(pokemonRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> pokemonService.deletePokemon(1L));
    }
}
