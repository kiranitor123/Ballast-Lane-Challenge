import { describe, it, expect, beforeEach } from 'vitest';
import { getToken, isAuthenticated, getCurrentUser, logout } from '../services/authService';

describe('authService storage utilities', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('retrieves token and checks authentication state', () => {
    expect(getToken()).toBeNull();
    expect(isAuthenticated()).toBe(false);

    localStorage.setItem('token', 'test-jwt-token');
    expect(getToken()).toBe('test-jwt-token');
    expect(isAuthenticated()).toBe(true);
  });

  it('saves and retrieves user data', () => {
    const user = { username: 'ash_ketchum', role: 'USER' };
    localStorage.setItem('user', JSON.stringify(user));
    expect(getCurrentUser()).toEqual(user);
  });

  it('returns null if user data is missing or corrupted', () => {
    expect(getCurrentUser()).toBeNull();
    localStorage.setItem('user', 'invalid-json{');
    expect(getCurrentUser()).toBeNull();
  });

  it('logout clears both token and user', () => {
    localStorage.setItem('token', 'some-token');
    localStorage.setItem('user', JSON.stringify({ username: 'misty', role: 'USER' }));
    logout();
    expect(getToken()).toBeNull();
    expect(getCurrentUser()).toBeNull();
    expect(isAuthenticated()).toBe(false);
  });
});
