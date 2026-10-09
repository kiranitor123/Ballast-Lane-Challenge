export interface Pokemon {
  id: number;
  pokeApiId: number;
  name: string;
  spriteUrl: string;
  weight: number;
  types: string[];
  abilities: string[];
}

export interface PokemonDetail extends Pokemon {
  height: number;
  stats: {
    hp: number;
    attack: number;
    defense: number;
    specialAttack: number;
    specialDefense: number;
    speed: number;
  };
  description: string;
  evolutionChain: EvolutionStage[];
  localName?: string;
  region?: string;
  customTag?: string;
}

export interface EvolutionStage {
  name: string;
  pokeApiId: number;
  spriteUrl: string;
  minLevel?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export interface User {
  id?: number;
  username: string;
  email?: string;
  role: string;
}
