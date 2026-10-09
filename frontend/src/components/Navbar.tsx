import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import SyncModal from './SyncModal';

const Navbar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isSyncModalOpen, setIsSyncModalOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <>
      <nav className="bg-primary text-white shadow-md sticky top-0 z-50">
        <div className="container mx-auto px-4">
          <div className="flex justify-between items-center h-16">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white rounded-full flex items-center justify-center">
                <div className="w-6 h-6 rounded-full border-2 border-primary border-t-secondary"></div>
              </div>
              <span className="text-xl font-bold tracking-wider">PokeDex</span>
            </Link>

            <div className="hidden md:flex items-center gap-6">
              <Link to="/" className="hover:text-gray-200 transition-colors font-medium">Home</Link>
              {isAuthenticated && (
                <button onClick={() => setIsSyncModalOpen(true)} className="hover:text-gray-200 transition-colors font-medium">
                  Sync Pokemon
                </button>
              )}
              
              <div className="h-6 w-px bg-red-400 mx-2"></div>
              
              {isAuthenticated ? (
                <div className="flex items-center gap-4">
                  <span className="text-sm bg-red-800 px-3 py-1 rounded-full">{user?.username}</span>
                  <button onClick={handleLogout} className="text-sm hover:text-gray-200 transition-colors">Logout</button>
                </div>
              ) : (
                <div className="flex items-center gap-4">
                  <Link to="/login" className="hover:text-gray-200 transition-colors">Login</Link>
                  <Link to="/register" className="bg-white text-primary px-4 py-2 rounded-lg font-medium hover:bg-gray-100 transition-colors">Register</Link>
                </div>
              )}
            </div>

            <div className="md:hidden flex items-center">
              <button onClick={() => setIsMenuOpen(!isMenuOpen)} className="text-white hover:text-gray-200 focus:outline-none">
                <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  {isMenuOpen ? (
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  ) : (
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  )}
                </svg>
              </button>
            </div>
          </div>
        </div>

        {isMenuOpen && (
          <div className="md:hidden bg-red-700 pb-4 px-4 space-y-3 shadow-inner">
            <Link to="/" className="block py-2 text-white hover:text-gray-200" onClick={() => setIsMenuOpen(false)}>Home</Link>
            {isAuthenticated && (
              <button 
                onClick={() => { setIsSyncModalOpen(true); setIsMenuOpen(false); }} 
                className="block py-2 w-full text-left text-white hover:text-gray-200"
              >
                Sync Pokemon
              </button>
            )}
            <div className="h-px bg-red-600 my-2"></div>
            {isAuthenticated ? (
              <>
                <div className="py-2 text-red-200">Logged in as <span className="font-bold text-white">{user?.username}</span></div>
                <button onClick={() => { handleLogout(); setIsMenuOpen(false); }} className="block py-2 w-full text-left text-white hover:text-gray-200">Logout</button>
              </>
            ) : (
              <>
                <Link to="/login" className="block py-2 text-white hover:text-gray-200" onClick={() => setIsMenuOpen(false)}>Login</Link>
                <Link to="/register" className="block py-2 text-white hover:text-gray-200" onClick={() => setIsMenuOpen(false)}>Register</Link>
              </>
            )}
          </div>
        )}
      </nav>

      <SyncModal 
        isOpen={isSyncModalOpen} 
        onClose={() => setIsSyncModalOpen(false)} 
        onSuccess={() => { window.location.reload(); }} 
      />
    </>
  );
};

export default Navbar;
