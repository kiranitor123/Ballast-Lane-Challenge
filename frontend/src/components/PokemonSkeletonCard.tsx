import React from 'react';

const PokemonSkeletonCard: React.FC = () => {
  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden animate-pulse">
      <div className="bg-gray-100 h-48 flex items-center justify-center p-6 relative">
        <div className="w-32 h-32 rounded-full bg-gray-200" />
      </div>

      <div className="p-4 text-center">
        <div className="h-5 bg-gray-200 rounded w-1/2 mx-auto mb-3" />
        
        <div className="flex justify-center gap-1.5 mb-3">
          <div className="h-5 w-16 bg-gray-200 rounded-full" />
          <div className="h-5 w-16 bg-gray-200 rounded-full" />
        </div>

        <div className="flex justify-between items-center px-2 pt-2 border-t border-gray-100">
          <div className="h-3 w-12 bg-gray-200 rounded" />
          <div className="h-3 w-16 bg-gray-200 rounded" />
        </div>
      </div>
    </div>
  );
};

export default PokemonSkeletonCard;
