import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import React from 'react';
import ScrollToTop from '../components/ScrollToTop';

describe('ScrollToTop Component', () => {
  beforeEach(() => {
    window.scrollY = 0;
    window.scrollTo = vi.fn();
  });

  it('is hidden when scrollY <= 300', () => {
    render(<ScrollToTop />);
    expect(screen.queryByRole('button', { name: /volver al inicio/i })).not.toBeInTheDocument();
  });

  it('becomes visible when scrollY > 300', () => {
    render(<ScrollToTop />);
    act(() => {
      window.scrollY = 350;
      window.dispatchEvent(new Event('scroll'));
    });
    expect(screen.getByRole('button', { name: /volver al inicio/i })).toBeInTheDocument();
  });

  it('scrolls smoothly to top when clicked', () => {
    render(<ScrollToTop />);
    act(() => {
      window.scrollY = 450;
      window.dispatchEvent(new Event('scroll'));
    });

    const button = screen.getByRole('button', { name: /volver al inicio/i });
    fireEvent.click(button);

    expect(window.scrollTo).toHaveBeenCalledWith({
      top: 0,
      behavior: 'smooth'
    });
  });
});
