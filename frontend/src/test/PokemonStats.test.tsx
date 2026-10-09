import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import PokemonStats from '../components/PokemonStats';

describe('PokemonStats Component', () => {
  const mockStats = {
    hp: 35,
    attack: 55,
    defense: 40,
    specialAttack: 50,
    specialDefense: 50,
    speed: 90
  };

  it('renders all core stat labels and values', () => {
    render(<PokemonStats stats={mockStats} />);
    expect(screen.getByText('HP')).toBeInTheDocument();
    expect(screen.getByText('35')).toBeInTheDocument();

    expect(screen.getByText('Attack')).toBeInTheDocument();
    expect(screen.getByText('55')).toBeInTheDocument();

    expect(screen.getByText('Defense')).toBeInTheDocument();
    expect(screen.getByText('40')).toBeInTheDocument();

    expect(screen.getByText('Sp. Atk')).toBeInTheDocument();
    expect(screen.getByText('Sp. Def')).toBeInTheDocument();
    expect(screen.getAllByText('50')).toHaveLength(2);

    expect(screen.getByText('Speed')).toBeInTheDocument();
    expect(screen.getByText('90')).toBeInTheDocument();
  });

  it('renders progress bars for each stat', () => {
    const { container } = render(<PokemonStats stats={mockStats} />);
    const bars = container.querySelectorAll('.bg-gray-200');
    expect(bars).toHaveLength(6);
  });
});
