import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import PageLoadingFallback from './PageLoadingFallback';

describe('PageLoadingFallback', () => {
  it('지연 로딩 상태를 보조 기술에 전달한다', () => {
    renderWithTheme(<PageLoadingFallback />);

    const status = screen.getByRole('status', { name: '페이지를 불러오는 중' });
    expect(status).toHaveAttribute('aria-live', 'polite');
    expect(within(status).getByRole('progressbar')).toBeInTheDocument();
    expect(within(status).getByText('페이지를 불러오는 중입니다.')).toBeVisible();
  });
});
