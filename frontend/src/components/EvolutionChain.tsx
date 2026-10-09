import React from 'react';
import { Link } from 'react-router-dom';
import type { EvolutionStage } from '../types';
import PokemonImage from './PokemonImage';

interface Props {
  chain: EvolutionStage[];
}

const EvolutionChain: React.FC<Props> = ({ chain }) => {
  if (!chain || chain.length <= 1) return <p className="text-gray-400 italic text-sm">Este Pokémon no cuenta con evoluciones conocidas.</p>;

  return (
    <div className="flex items-center justify-start flex-wrap gap-4 py-4">
      {chain.map((stage, index) => (
        <React.Fragment key={stage.pokeApiId}>
          <Link to={`/pokemon/${stage.pokeApiId}`} className="group flex flex-col items-center">
            <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center p-2 border-2 border-gray-100 group-hover:border-red-500 group-hover:shadow-lg transition-all">
              <PokemonImage
                src={stage.spriteUrl}
                alt={stage.name}
                className="w-16 h-16"
                imgClassName="w-16 h-16 object-contain group-hover:scale-110 transition-transform"
              />
            </div>
            <span className="mt-2 text-sm font-semibold capitalize text-gray-700 group-hover:text-red-600 transition-colors">
              {stage.name}
            </span>
          </Link>
          {index < chain.length - 1 && (
            <div className="flex flex-col items-center px-2">
              <span className="text-gray-300 text-2xl font-bold">→</span>
              {chain[index + 1].minLevel && (
                <span className="text-xs text-gray-400 font-medium bg-gray-100 px-2 py-0.5 rounded-full mt-0.5">
                  Nivel {chain[index + 1].minLevel}
                </span>
              )}
            </div>
          )}
        </React.Fragment>
      ))}
    </div>
  );
};

export default EvolutionChain;
