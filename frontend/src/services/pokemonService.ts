import api from './api';
import type { PageResponse, Pokemon, PokemonDetail } from '../types';

export const getPokemons = async (page: number = 0, size: number = 20, search?: string): Promise<PageResponse<Pokemon>> => {
  const response = await api.get<PageResponse<Pokemon>>('/pokemon', {
    params: { page, size, search }
  });
  return response.data;
};

export const getPokemonDetail = async (id: number): Promise<PokemonDetail> => {
  const response = await api.get<PokemonDetail>(`/pokemon/${id}`);
  return response.data;
};

export const syncPokemon = async (identifier: number | string): Promise<PokemonDetail> => {
  const response = await api.post<PokemonDetail>(`/pokemon/sync/${identifier}`);
  return response.data;
};

export const updatePokemon = async (id: number, data: Partial<PokemonDetail>): Promise<PokemonDetail> => {
  const response = await api.put<PokemonDetail>(`/pokemon/${id}`, data);
  return response.data;
};

export const deletePokemon = async (id: number): Promise<void> => {
  await api.delete(`/pokemon/${id}`);
};
