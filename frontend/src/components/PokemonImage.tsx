import React, { useState } from 'react';

interface Props {
  src?: string;
  alt: string;
  className?: string;
  imgClassName?: string;
}

const PokemonImage: React.FC<Props> = ({ src, alt, className = '', imgClassName = '' }) => {
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  const fallbackUrl = 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png';
  const effectiveSrc = !src || hasError ? fallbackUrl : src;

  return (
    <div className={`relative flex items-center justify-center ${className}`}>
      {isLoading && (
        <div className="absolute inset-2 bg-gray-200/80 rounded-full animate-pulse pointer-events-none" />
      )}

      <img
        src={effectiveSrc}
        alt={alt}
        loading="lazy"
        onLoad={() => setIsLoading(false)}
        onError={() => {
          setIsLoading(false);
          setHasError(true);
        }}
        className={`transition-opacity duration-200 ${
          isLoading ? 'opacity-0' : 'opacity-100'
        } ${imgClassName}`}
      />
    </div>
  );
};

export default PokemonImage;
