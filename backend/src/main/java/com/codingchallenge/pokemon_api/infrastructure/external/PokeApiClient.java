package com.codingchallenge.pokemon_api.infrastructure.external;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class PokeApiClient {

    private final RestTemplate restTemplate;

    public PokeApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${pokeapi.base-url}")
    private String baseUrl;

    @Cacheable("pokeapi-cache")
    public JsonNode getPokemon(int id) {
        return getPokemon(String.valueOf(id));
    }

    @Cacheable("pokeapi-cache")
    public JsonNode getPokemon(String nameOrId) {
        try {
            return restTemplate.getForObject(baseUrl + "/pokemon/" + nameOrId.toLowerCase().trim(), JsonNode.class);
        } catch (RestClientException e) {
            throw new PokeApiException("Failed to fetch pokemon from PokeAPI: " + nameOrId, e);
        }
    }

    @Cacheable("pokeapi-cache")
    public JsonNode getPokemonSpecies(int id) {
        try {
            return restTemplate.getForObject(baseUrl + "/pokemon-species/" + id, JsonNode.class);
        } catch (RestClientException e) {
            throw new PokeApiException("Failed to fetch pokemon species from PokeAPI: " + id, e);
        }
    }

    @Cacheable("pokeapi-cache")
    public JsonNode getEvolutionChain(String url) {
        try {
            return restTemplate.getForObject(url, JsonNode.class);
        } catch (RestClientException e) {
            throw new PokeApiException("Failed to fetch evolution chain", e);
        }
    }

    @Cacheable("pokeapi-cache")
    public JsonNode getPokemonList(int offset, int limit) {
        try {
            return restTemplate.getForObject(baseUrl + "/pokemon?offset=" + offset + "&limit=" + limit, JsonNode.class);
        } catch (RestClientException e) {
            throw new PokeApiException("Failed to fetch pokemon list", e);
        }
    }
}
