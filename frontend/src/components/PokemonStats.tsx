import React, { useEffect, useState } from 'react';

interface Props {
  stats: {
    hp: number;
    attack: number;
    defense: number;
    specialAttack: number;
    specialDefense: number;
    speed: number;
  };
}

const PokemonStats: React.FC<Props> = ({ stats }) => {
  const maxStat = 255;
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const getStatColor = (val: number) => {
    if (val < 50) return 'bg-red-500';
    if (val < 100) return 'bg-yellow-400';
    return 'bg-green-500';
  };

  const statNames = [
    { key: 'hp', label: 'HP', val: stats.hp },
    { key: 'attack', label: 'Attack', val: stats.attack },
    { key: 'defense', label: 'Defense', val: stats.defense },
    { key: 'specialAttack', label: 'Sp. Atk', val: stats.specialAttack },
    { key: 'specialDefense', label: 'Sp. Def', val: stats.specialDefense },
    { key: 'speed', label: 'Speed', val: stats.speed },
  ];

  return (
    <div className="w-full space-y-3">
      {statNames.map((s) => (
        <div key={s.key} className="flex items-center">
          <div className="w-24 text-sm font-medium text-gray-600">{s.label}</div>
          <div className="w-8 text-sm text-right pr-2 text-gray-700">{s.val}</div>
          <div className="flex-1 h-3 bg-gray-200 rounded-full overflow-hidden">
            <div 
              className={`h-full rounded-full transition-all duration-1000 ease-out ${getStatColor(s.val)}`}
              style={{ width: mounted ? `${(s.val / maxStat) * 100}%` : '0%' }}
            ></div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default PokemonStats;
