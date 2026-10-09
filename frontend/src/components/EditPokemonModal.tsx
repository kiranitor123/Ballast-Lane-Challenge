import React, { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';
import { updatePokemon } from '../services/pokemonService';
import type { PokemonDetail } from '../types';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  pokemon: PokemonDetail;
  onSuccess: (updated: PokemonDetail) => void;
}

const EditPokemonModal: React.FC<Props> = ({ isOpen, onClose, pokemon, onSuccess }) => {
  const [formData, setFormData] = useState({
    name: '',
    localName: '',
    region: '',
    customTag: ''
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && pokemon) {
      setFormData({
        name: pokemon.name || '',
        localName: pokemon.localName || '',
        region: pokemon.region || '',
        customTag: pokemon.customTag || ''
      });
    }
  }, [isOpen, pokemon]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name) {
      toast.error('Name is required');
      return;
    }

    try {
      setLoading(true);
      const updated = await updatePokemon(pokemon.id, formData);
      toast.success('Pokemon updated successfully!');
      onSuccess(updated);
      onClose();
    } catch (err) {
      toast.error('Failed to update Pokemon.');
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
        <h2 className="text-2xl font-bold mb-6 text-gray-800">Edit {pokemon.name}</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
            <input 
              type="text" 
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Local Name</label>
            <input 
              type="text" 
              value={formData.localName}
              onChange={(e) => setFormData({...formData, localName: e.target.value})}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Region</label>
            <input 
              type="text" 
              value={formData.region}
              onChange={(e) => setFormData({...formData, region: e.target.value})}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Custom Tag</label>
            <input 
              type="text" 
              value={formData.customTag}
              onChange={(e) => setFormData({...formData, customTag: e.target.value})}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none"
            />
          </div>
          <div className="flex justify-end gap-3 mt-8 pt-4">
            <button 
              type="button" 
              onClick={onClose}
              className="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={loading}
              className="px-4 py-2 bg-primary text-white rounded-lg hover:bg-red-700 disabled:opacity-50 flex items-center gap-2 transition-colors"
            >
              {loading ? <span className="animate-spin inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full"></span> : null}
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditPokemonModal;
