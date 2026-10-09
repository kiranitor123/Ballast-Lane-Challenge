package com.codingchallenge.pokemon_api.presentation.controller;

import tools.jackson.databind.ObjectMapper;
import com.codingchallenge.pokemon_api.application.dto.PageResponseDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonDetailDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonListDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonSyncRequestDto;
import com.codingchallenge.pokemon_api.application.dto.PokemonUpdateDto;
import com.codingchallenge.pokemon_api.application.service.PokemonService;
import com.codingchallenge.pokemon_api.infrastructure.security.JwtService;
import com.codingchallenge.pokemon_api.presentation.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PokemonController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonService pokemonService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private PokemonDetailDto mockDetailDto;

    @BeforeEach
    void setUp() {
        mockDetailDto = new PokemonDetailDto(
                1L,
                1,
                "bulbasaur",
                "https://example.com/front.png",
                69,
                7,
                List.of("grass", "poison"),
                List.of("overgrow"),
                Map.of("hp", 45, "attack", 49),
                "A seed Pokemon.",
                Collections.emptyList(),
                "Fushigidane",
                "Kanto",
                "Starter"
        );
    }

    @Test
    void testListPokemon_ReturnsOk() throws Exception {
        PokemonListDto item = new PokemonListDto(
                1L, 1, "bulbasaur", "https://example.com/front.png", 69, List.of("grass"), List.of("overgrow")
        );
        PageResponseDto<PokemonListDto> page = new PageResponseDto<>(
                List.of(item), 0, 10, 1L, 1
        );
        when(pokemonService.listPokemon(anyInt(), anyInt(), any())).thenReturn(page);

        mockMvc.perform(get("/api/pokemon")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetPokemonDetail_ReturnsOk() throws Exception {
        when(pokemonService.getPokemonDetail(1L)).thenReturn(mockDetailDto);

        mockMvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bulbasaur"))
                .andExpect(jsonPath("$.localName").value("Fushigidane"));
    }

    @Test
    void testGetPokemonDetail_NotFound_Returns404() throws Exception {
        when(pokemonService.getPokemonDetail(1L)).thenThrow(new ResourceNotFoundException("Pokemon not found with id: 1"));

        mockMvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser
    void testSyncPokemon_WithBody_ReturnsCreated() throws Exception {
        PokemonSyncRequestDto syncRequest = new PokemonSyncRequestDto(1);
        when(pokemonService.syncPokemon(1)).thenReturn(mockDetailDto);

        mockMvc.perform(post("/api/pokemon/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("bulbasaur"));
    }

    @Test
    @WithMockUser
    void testSyncPokemon_WithPath_ReturnsCreated() throws Exception {
        when(pokemonService.syncPokemon(1)).thenReturn(mockDetailDto);

        mockMvc.perform(post("/api/pokemon/sync/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("bulbasaur"));
    }

    @Test
    @WithMockUser
    void testSyncPokemon_WithNamePath_ReturnsCreated() throws Exception {
        when(pokemonService.syncPokemon("pikachu")).thenReturn(mockDetailDto);

        mockMvc.perform(post("/api/pokemon/sync/pikachu"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("bulbasaur"));
    }

    @Test
    @WithMockUser
    void testUpdatePokemon_WithAuth_ReturnsOk() throws Exception {
        PokemonUpdateDto updateDto = new PokemonUpdateDto("Bulbasaur", "Custom Local", "Kanto", "Tag");
        when(pokemonService.updatePokemon(eq(1L), any(PokemonUpdateDto.class))).thenReturn(mockDetailDto);

        mockMvc.perform(put("/api/pokemon/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bulbasaur"));
    }

    @Test
    @WithMockUser
    void testUpdatePokemon_InvalidBody_Returns400() throws Exception {
        PokemonUpdateDto invalidDto = new PokemonUpdateDto("", "Local", "Region", "Tag");

        mockMvc.perform(put("/api/pokemon/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @WithMockUser
    void testDeletePokemon_WithAuth_Returns204() throws Exception {
        doNothing().when(pokemonService).deletePokemon(1L);

        mockMvc.perform(delete("/api/pokemon/1"))
                .andExpect(status().isNoContent());

        verify(pokemonService).deletePokemon(1L);
    }
}
