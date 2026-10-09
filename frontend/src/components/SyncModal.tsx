import React, { useState } from 'react';
import { toast } from 'react-hot-toast';
import { syncPokemon } from '../services/pokemonService';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const SyncModal: React.FC<Props> = ({ isOpen, onClose, onSuccess }) => {
  const [pokeApiId, setPokeApiId] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSync = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!pokeApiId) return;

    try {
      setLoading(true);
      await syncPokemon(pokeApiId.trim());
      toast.success('Pokemon synced successfully!');
      onSuccess();
      onClose();
      setPokeApiId('');
    } catch (err) {
      toast.error('Failed to sync Pokemon. It might already exist or not be found.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6 relative">
        <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
          ✕
        </button>
        <h2 className="text-2xl font-bold mb-6 text-gray-800">Sync Pokemon</h2>
        <form onSubmit={handleSync}>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">PokeAPI ID or Name</label>
            <input 
              type="text" 
              value={pokeApiId}
              onChange={(e) => setPokeApiId(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none"
              placeholder="e.g. 25 or pikachu"
              required
            />
          </div>
          <div className="flex justify-end gap-3 mt-8">
            <button 
              type="button" 
              onClick={onClose}
              className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={loading || !pokeApiId}
              className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-red-700 disabled:opacity-50 flex items-center gap-2 transition-colors"
            >
              {loading ? <span className="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> : null}
              {loading ? 'Syncing...' : 'Sync Pokemon'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SyncModal;
