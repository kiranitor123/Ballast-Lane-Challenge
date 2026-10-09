import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { getPokemonDetail, deletePokemon } from '../services/pokemonService';
import type { PokemonDetail as PokemonDetailType } from '../types';
import { useAuth } from '../context/AuthContext';
import TypeBadge from '../components/TypeBadge';
import PokemonStats from '../components/PokemonStats';
import EvolutionChain from '../components/EvolutionChain';
import LoadingSpinner from '../components/LoadingSpinner';
import EditPokemonModal from '../components/EditPokemonModal';
import PokemonImage from '../components/PokemonImage';

const PokemonDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  
  const [pokemon, setPokemon] = useState<PokemonDetailType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        if (!id) return;
        setLoading(true);
        const data = await getPokemonDetail(Number(id));
        setPokemon(data);
      } catch (err) {
        setError('Failed to load Pokemon details.');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

  const handleDelete = async () => {
    if (!pokemon) return;
    if (window.confirm(`Are you sure you want to delete ${pokemon.name}?`)) {
      try {
        await deletePokemon(pokemon.id);
        toast.success(`${pokemon.name} deleted successfully!`);
        navigate('/');
      } catch (err) {
        toast.error('Failed to delete Pokemon.');
      }
    }
  };

  if (loading) {
    return (
      <div className="py-16">
        <LoadingSpinner message="Cargando detalles del Pokémon..." />
      </div>
    );
  }

  if (error || !pokemon) {
    return (
      <div className="max-w-md mx-auto my-12 p-8 bg-white rounded-xl shadow-xs border border-gray-200 text-center">
        <h2 className="text-lg font-bold text-gray-900 mb-1">Pokémon no encontrado</h2>
        <p className="text-gray-500 mb-6 text-sm">{error || 'No se pudo cargar la información de este Pokémon.'}</p>
        <button
          onClick={() => navigate('/')}
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm font-medium"
        >
          Volver a la Pokédex
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto pb-12">
      <button
        onClick={() => navigate('/')}
        className="text-gray-600 hover:text-red-600 mb-6 inline-flex items-center gap-1.5 transition-colors text-sm font-medium focus:outline-none"
      >
        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span>Volver a la lista</span>
      </button>

      <div className="bg-white rounded-xl shadow-xs overflow-hidden border border-gray-200">
        <div className="md:flex">
          <div className="md:w-1/3 bg-gray-50 p-8 flex flex-col items-center border-r border-gray-200">
            <span className="text-gray-400 font-mono text-xs font-semibold mb-3">
              #{String(pokemon.pokeApiId).padStart(3, '0')}
            </span>
            <PokemonImage 
              src={pokemon.spriteUrl} 
              alt={pokemon.name}
              className="w-48 h-48 mb-6"
              imgClassName="w-44 h-44 object-contain"
            />
            <h1 className="text-2xl font-bold capitalize text-gray-900 mb-3">{pokemon.name}</h1>
            <div className="flex gap-2 mb-6">
              {pokemon.types.map(t => <TypeBadge key={t} type={t} />)}
            </div>
            <div className="w-full bg-white rounded-xl p-4 shadow-sm border border-gray-100">
              <div className="flex justify-between py-2 border-b border-gray-50">
                <span className="text-gray-500 text-sm">Height</span>
                <span className="font-medium text-gray-800">{pokemon.height / 10} m</span>
              </div>
              <div className="flex justify-between py-2 border-b border-gray-50">
                <span className="text-gray-500 text-sm">Weight</span>
                <span className="font-medium text-gray-800">{pokemon.weight / 10} kg</span>
              </div>
              <div className="flex justify-between py-2">
                <span className="text-gray-500 text-sm">Abilities</span>
                <span className="font-medium text-gray-800 text-right capitalize">
                  {pokemon.abilities.join(', ')}
                </span>
              </div>
            </div>
          </div>

          <div className="md:w-2/3 p-8">
            <div className="flex justify-between items-start mb-6">
              <h2 className="text-2xl font-bold text-gray-800">Description</h2>
              {isAuthenticated && (
                <div className="flex gap-2">
                  <button 
                    onClick={() => setIsEditModalOpen(true)}
                    className="px-3 py-1.5 text-sm bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-md transition-colors"
                  >
                    Edit
                  </button>
                  <button 
                    onClick={handleDelete}
                    className="px-3 py-1.5 text-sm bg-red-50 text-red-600 hover:bg-red-100 rounded-md transition-colors"
                  >
                    Delete
                  </button>
                </div>
              )}
            </div>
            
            <p className="text-gray-600 mb-8 leading-relaxed">
              {pokemon.description || 'No description available for this Pokemon.'}
            </p>

            {(pokemon.localName || pokemon.region || pokemon.customTag) && (
              <div className="mb-8 p-4 bg-yellow-50 border border-yellow-100 rounded-xl">
                <h3 className="font-semibold text-yellow-800 mb-2">Custom Information</h3>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  {pokemon.localName && <div><span className="text-yellow-600 block">Local Name</span> <span className="font-medium">{pokemon.localName}</span></div>}
                  {pokemon.region && <div><span className="text-yellow-600 block">Region</span> <span className="font-medium">{pokemon.region}</span></div>}
                  {pokemon.customTag && <div className="col-span-2"><span className="text-yellow-600 block">Tag</span> <span className="inline-block bg-white px-2 py-1 rounded text-yellow-800 border border-yellow-200 mt-1">{pokemon.customTag}</span></div>}
                </div>
              </div>
            )}

            <div className="mb-8">
              <h2 className="text-xl font-bold text-gray-800 mb-4">Base Stats</h2>
              <PokemonStats stats={pokemon.stats} />
            </div>

            <div>
              <h2 className="text-xl font-bold text-gray-800 mb-2">Evolutions</h2>
              <EvolutionChain chain={pokemon.evolutionChain} />
            </div>
          </div>
        </div>
      </div>

      <EditPokemonModal 
        isOpen={isEditModalOpen} 
        onClose={() => setIsEditModalOpen(false)} 
        pokemon={pokemon}
        onSuccess={setPokemon}
      />
    </div>
  );
};

export default PokemonDetail;
