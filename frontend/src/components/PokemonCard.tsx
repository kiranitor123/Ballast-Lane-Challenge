import React from 'react';
import { Link } from 'react-router-dom';
import type { Pokemon } from '../types';
import TypeBadge from './TypeBadge';
import PokemonImage from './PokemonImage';

interface Props {
  pokemon: Pokemon;
}

const PokemonCard: React.FC<Props> = ({ pokemon }) => {
  return (
    <Link to={`/pokemon/${pokemon.id}`} className="block group focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500 rounded-xl">
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs hover:shadow-md hover:border-gray-300 transition-all duration-150 overflow-hidden">
        <div className="bg-gray-50 flex justify-center items-center p-6 relative h-48 border-b border-gray-100">
          <span className="absolute top-2.5 left-3 text-gray-400 font-mono text-xs font-medium">
            #{String(pokemon.pokeApiId).padStart(3, '0')}
          </span>
          <PokemonImage 
            src={pokemon.spriteUrl} 
            alt={pokemon.name}
            className="w-36 h-36"
            imgClassName="w-36 h-36 object-contain"
          />
        </div>

        <div className="p-4 text-center">
          <h2 className="text-base font-bold capitalize text-gray-900 mb-2 group-hover:text-red-600 transition-colors">
            {pokemon.name}
          </h2>
          <div className="flex justify-center gap-1.5 mb-3 flex-wrap">
            {pokemon.types.map(t => <TypeBadge key={t} type={t} />)}
          </div>
          <div className="text-xs text-gray-500 flex justify-between px-2 pt-2 border-t border-gray-100 font-medium">
            <span>{pokemon.weight / 10} kg</span>
            <span className="capitalize">{pokemon.abilities[0] || '—'}</span>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default PokemonCard;
