import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import ListSurfaceToolbar from './ListSurfaceToolbar';

describe('ListSurfaceToolbar', () => {
  it('기본값은 상단 행을 표시한다', () => {
    renderWithTheme(
      <ListSurfaceToolbar>
        <span>필터</span>
      </ListSurfaceToolbar>,
    );

    expect(screen.getByText('필터')).toBeInTheDocument();
  });

  it('불필요한 화면에서는 상단 행 자체를 비활성화한다', () => {
    renderWithTheme(
      <ListSurfaceToolbar visible={false}>
        <span>중복 타이틀</span>
      </ListSurfaceToolbar>,
    );

    expect(screen.queryByText('중복 타이틀')).not.toBeInTheDocument();
  });
});
