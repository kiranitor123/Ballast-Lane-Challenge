import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import TypeBadge from '../components/TypeBadge';

describe('TypeBadge Component', () => {
  it('renders type name capitalized', () => {
    render(<TypeBadge type="electric" />);
    const badge = screen.getByText('electric');
    expect(badge).toBeInTheDocument();
  });

  it('applies electric style correctly', () => {
    const { container } = render(<TypeBadge type="electric" />);
    expect(container.firstChild).toHaveStyle({ backgroundColor: '#F8D030' });
  });

  it('applies fire style correctly', () => {
    const { container } = render(<TypeBadge type="fire" />);
    expect(container.firstChild).toHaveStyle({ backgroundColor: '#F08030' });
  });

  it('applies water style correctly', () => {
    const { container } = render(<TypeBadge type="water" />);
    expect(container.firstChild).toHaveStyle({ backgroundColor: '#6890F0' });
  });

  it('falls back to default gray style for unknown types', () => {
    const { container } = render(<TypeBadge type="unknown-custom-type" />);
    expect(container.firstChild).toHaveStyle({ backgroundColor: '#A8A878' });
  });
});
