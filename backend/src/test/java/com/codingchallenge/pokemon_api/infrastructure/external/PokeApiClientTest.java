package com.codingchallenge.pokemon_api.infrastructure.external;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PokeApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PokeApiClient pokeApiClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pokeApiClient, "baseUrl", "https://pokeapi.co/api/v2");
    }

    @Test
    void testGetPokemon_Success_ReturnsJsonNode() throws Exception {
        String json = "{\"id\": 1, \"name\": \"bulbasaur\"}";
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject("https://pokeapi.co/api/v2/pokemon/1", JsonNode.class))
                .thenReturn(node);

        JsonNode result = pokeApiClient.getPokemon(1);
        assertNotNull(result);
        assertEquals("bulbasaur", result.get("name").asString());
    }

    @Test
    void testGetPokemonByName_Success_ReturnsJsonNode() throws Exception {
        String json = "{\"id\": 25, \"name\": \"pikachu\"}";
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject("https://pokeapi.co/api/v2/pokemon/pikachu", JsonNode.class))
                .thenReturn(node);

        JsonNode result = pokeApiClient.getPokemon("pikachu");
        assertNotNull(result);
        assertEquals("pikachu", result.get("name").asString());
    }

    @Test
    void testGetPokemon_ApiError_ThrowsPokeApiException() {
        when(restTemplate.getForObject("https://pokeapi.co/api/v2/pokemon/99999", JsonNode.class))
                .thenThrow(new RestClientException("Not Found"));

        assertThrows(PokeApiException.class, () -> pokeApiClient.getPokemon(99999));
    }

    @Test
    void testGetPokemonSpecies_Success_ReturnsJsonNode() throws Exception {
        String json = "{\"flavor_text_entries\": []}";
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject("https://pokeapi.co/api/v2/pokemon-species/1", JsonNode.class))
                .thenReturn(node);

        JsonNode result = pokeApiClient.getPokemonSpecies(1);
        assertNotNull(result);
    }

    @Test
    void testGetEvolutionChain_Success_ReturnsJsonNode() throws Exception {
        String json = "{\"chain\": {}}";
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject("https://pokeapi.co/api/v2/evolution-chain/1", JsonNode.class))
                .thenReturn(node);

        JsonNode result = pokeApiClient.getEvolutionChain("https://pokeapi.co/api/v2/evolution-chain/1");
        assertNotNull(result);
    }

    @Test
    void testGetPokemonList_Success_ReturnsJsonNode() throws Exception {
        String json = "{\"results\": []}";
        JsonNode node = objectMapper.readTree(json);

        when(restTemplate.getForObject("https://pokeapi.co/api/v2/pokemon?offset=0&limit=20", JsonNode.class))
                .thenReturn(node);

        JsonNode result = pokeApiClient.getPokemonList(0, 20);
        assertNotNull(result);
    }
}
