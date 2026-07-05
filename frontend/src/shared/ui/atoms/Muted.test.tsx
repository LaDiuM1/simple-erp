import { describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
import { renderWithTheme } from '@/test/renderWithTheme';
import Muted from './Muted';

describe('Muted', () => {
  it('children 미지정 시 기본 placeholder "-" 렌더', () => {
    renderWithTheme(<Muted />);
    expect(screen.getByText('-')).toBeInTheDocument();
  });

  it('children 지정 시 해당 텍스트 렌더', () => {
    renderWithTheme(<Muted>미등록</Muted>);
    expect(screen.getByText('미등록')).toBeInTheDocument();
  });
});
