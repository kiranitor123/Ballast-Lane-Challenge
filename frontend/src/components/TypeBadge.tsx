import React from 'react';

const typeColors: Record<string, string> = {
  normal: '#A8A878',
  fire: '#F08030',
  water: '#6890F0',
  grass: '#78C850',
  electric: '#F8D030',
  ice: '#98D8D8',
  fighting: '#C03028',
  poison: '#A040A0',
  ground: '#E0C068',
  flying: '#A890F0',
  psychic: '#F85888',
  bug: '#A8B820',
  rock: '#B8A038',
  ghost: '#705898',
  dragon: '#7038F8',
  dark: '#705848',
  steel: '#B8B8D0',
  fairy: '#EE99AC'
};

interface TypeBadgeProps {
  type: string;
}

const TypeBadge: React.FC<TypeBadgeProps> = ({ type }) => {
  const color = typeColors[type.toLowerCase()] || '#A8A878';
  return (
    <span 
      className="px-2 py-1 rounded-full text-xs font-semibold text-white uppercase tracking-wider shadow-sm"
      style={{ backgroundColor: color }}
    >
      {type}
    </span>
  );
};

export default TypeBadge;
