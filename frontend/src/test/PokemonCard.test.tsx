import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import React from 'react';
import PokemonCard from '../components/PokemonCard';
import type { Pokemon } from '../types';

describe('PokemonCard Component', () => {
  const mockPokemon: Pokemon = {
    id: 1,
    pokeApiId: 25,
    name: 'pikachu',
    spriteUrl: 'https://example.com/pikachu.png',
    weight: 60,
    types: ['electric'],
    abilities: ['static', 'lightning-rod']
  };

  it('renders pokemon information with formatted id, weight and abilities', () => {
    render(
      <BrowserRouter>
        <PokemonCard pokemon={mockPokemon} />
      </BrowserRouter>
    );

    expect(screen.getByText('pikachu')).toBeInTheDocument();
    expect(screen.getByText('#025')).toBeInTheDocument();
    expect(screen.getByText('6 kg')).toBeInTheDocument();
    expect(screen.getByText('static')).toBeInTheDocument();
    expect(screen.getByText('electric')).toBeInTheDocument();
  });

  it('links to the pokemon detail page', () => {
    render(
      <BrowserRouter>
        <PokemonCard pokemon={mockPokemon} />
      </BrowserRouter>
    );

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/pokemon/1');
  });
});
