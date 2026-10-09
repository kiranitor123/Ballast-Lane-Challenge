import React from 'react';

interface Props {
  message?: string;
  fullScreen?: boolean;
}

const LoadingSpinner: React.FC<Props> = ({ message = 'Cargando datos...', fullScreen = false }) => {
  const content = (
    <div className="flex flex-col items-center justify-center p-8 gap-3" role="status" aria-live="polite">
      <div className="relative w-12 h-12 rounded-full border-2 border-gray-800 bg-white overflow-hidden shadow-sm animate-spin [animation-duration:1.5s]">
        <div className="absolute top-0 left-0 right-0 h-1/2 bg-red-600 border-b border-gray-800" />
        <div className="absolute bottom-0 left-0 right-0 h-1/2 bg-white" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-4 h-4 rounded-full bg-gray-800 flex items-center justify-center z-10">
          <div className="w-2 h-2 rounded-full bg-white border border-gray-800" />
        </div>
      </div>

      <p className="text-sm font-medium text-gray-700">{message}</p>
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white/90 backdrop-blur-xs flex items-center justify-center z-50">
        {content}
      </div>
    );
  }

  return content;
};

export default LoadingSpinner;
