import React, { useEffect, useState, useRef, useCallback } from 'react';
import { getPokemons } from '../services/pokemonService';
import type { Pokemon } from '../types';
import PokemonCard from '../components/PokemonCard';
import PokemonSkeletonCard from '../components/PokemonSkeletonCard';
import SyncModal from '../components/SyncModal';
import { useAuth } from '../context/AuthContext';

const PokemonList: React.FC = () => {
  const [pokemons, setPokemons] = useState<Pokemon[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [isSyncModalOpen, setIsSyncModalOpen] = useState(false);
  const { isAuthenticated } = useAuth();
  const observerTarget = useRef<HTMLDivElement>(null);

  const fetchInitialPokemons = async (searchQuery: string) => {
    try {
      setLoading(true);
      setError('');
      setPage(0);
      const data = await getPokemons(0, 20, searchQuery || undefined);
      setPokemons(data.content);
      setHasMore(data.content.length > 0 && data.page + 1 < data.totalPages);
    } catch (err) {
      setError('No se pudo conectar con el servicio. Por favor, reintenta en unos instantes.');
      setHasMore(false);
    } finally {
      setLoading(false);
    }
  };

  const loadMorePokemons = useCallback(async () => {
    if (loading || loadingMore || !hasMore) return;
    try {
      setLoadingMore(true);
      const nextPage = page + 1;
      const data = await getPokemons(nextPage, 20, search || undefined);
      if (data.content && data.content.length > 0) {
        setPokemons((prev) => {
          const existingIds = new Set(prev.map((p) => p.id));
          const uniqueNew = data.content.filter((p) => !existingIds.has(p.id));
          return [...prev, ...uniqueNew];
        });
        setPage(nextPage);
        if (nextPage + 1 >= data.totalPages || data.content.length < 20) {
          setHasMore(false);
        }
      } else {
        setHasMore(false);
      }
    } catch (err) {
      setHasMore(false);
    } finally {
      setLoadingMore(false);
    }
  }, [loading, loadingMore, hasMore, page, search]);

  useEffect(() => {
    fetchInitialPokemons(search);
  }, [search]);

  useEffect(() => {
    const target = observerTarget.current;
    if (!target) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading && !loadingMore) {
          loadMorePokemons();
        }
      },
      { threshold: 0.1, rootMargin: '300px' }
    );

    observer.observe(target);
    return () => {
      observer.unobserve(target);
    };
  }, [hasMore, loading, loadingMore, loadMorePokemons]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(searchInput.trim());
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setSearch('');
  };

  return (
    <div className="pb-16 max-w-7xl mx-auto px-4 sm:px-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8 pt-4">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">
            Biblioteca Pokémon
          </h1>
          <p className="text-gray-600 text-sm mt-1">
            Catálogo local de Pokémon replicados y sincronizados desde PokeAPI.
          </p>
        </div>

        {isAuthenticated && (
          <div>
            <button
              onClick={() => setIsSyncModalOpen(true)}
              className="inline-flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 active:bg-red-800 text-white rounded-lg text-sm font-medium transition-colors shadow-xs"
            >
              <span>+</span>
              <span>Sincronizar Pokémon</span>
            </button>
          </div>
        )}
      </div>

      <div className="mb-8">
        <form onSubmit={handleSearchSubmit} className="flex gap-2 max-w-lg">
          <div className="relative flex-1">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Buscar por nombre (ej. pikachu)..."
              className="w-full pl-9 pr-9 py-2 bg-white border border-gray-300 rounded-lg text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 shadow-xs"
            />
            {searchInput && (
              <button
                type="button"
                onClick={handleClearSearch}
                aria-label="Limpiar búsqueda"
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
          <button
            type="submit"
            className="px-4 py-2 bg-gray-900 hover:bg-gray-800 text-white rounded-lg text-sm font-medium transition-colors shadow-xs"
          >
            Buscar
          </button>
        </form>

        {search && (
          <div className="mt-2.5 flex items-center gap-2 text-xs text-gray-600">
            <span>Resultados para: <strong className="text-gray-900">"{search}"</strong></span>
            <span className="text-gray-300">•</span>
            <button
              onClick={handleClearSearch}
              className="text-red-600 hover:underline font-medium"
            >
              Restablecer lista completa
            </button>
          </div>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 flex items-center justify-between text-sm">
          <span>{error}</span>
          <button
            onClick={() => fetchInitialPokemons(search)}
            className="text-xs bg-red-600 text-white px-3 py-1.5 rounded font-medium hover:bg-red-700 transition-colors"
          >
            Reintentar
          </button>
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {Array.from({ length: 8 }).map((_, index) => (
            <PokemonSkeletonCard key={index} />
          ))}
        </div>
      ) : pokemons.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-10 text-center max-w-md mx-auto my-8">
          <div className="w-12 h-12 mx-auto mb-3 rounded-full bg-gray-100 flex items-center justify-center text-gray-500">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <h2 className="text-base font-semibold text-gray-900 mb-1">
            {search ? 'Sin coincidencias' : 'No hay Pokémon registrados'}
          </h2>
          <p className="text-gray-500 text-sm mb-6">
            {search
              ? `No se encontró ningún Pokémon con el nombre "${search}".`
              : 'La base de datos local está vacía. Inicia sincronización para poblar el catálogo.'}
          </p>
          <div className="flex justify-center gap-3">
            {search ? (
              <button
                onClick={handleClearSearch}
                className="px-4 py-2 border border-gray-300 text-gray-700 hover:bg-gray-50 rounded-lg text-sm font-medium transition-colors"
              >
                Limpiar búsqueda
              </button>
            ) : isAuthenticated ? (
              <button
                onClick={() => setIsSyncModalOpen(true)}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium transition-colors shadow-xs"
              >
                Sincronizar primer Pokémon
              </button>
            ) : (
              <span className="text-xs text-gray-400">Inicia sesión para sincronizar datos</span>
            )}
          </div>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {pokemons.map((p) => (
              <PokemonCard key={p.id} pokemon={p} />
            ))}
          </div>

          {loadingMore && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mt-6">
              {Array.from({ length: 4 }).map((_, index) => (
                <PokemonSkeletonCard key={`more-${index}`} />
              ))}
            </div>
          )}

          <div ref={observerTarget} className="h-12 mt-6 flex items-center justify-center">
            {!hasMore && pokemons.length > 0 && (
              <p className="text-xs text-gray-400 font-medium">
                Has llegado al final de los Pokémon disponibles.
              </p>
            )}
          </div>
        </>
      )}

      <SyncModal
        isOpen={isSyncModalOpen}
        onClose={() => setIsSyncModalOpen(false)}
        onSuccess={() => fetchInitialPokemons(search)}
      />
    </div>
  );
};

export default PokemonList;
