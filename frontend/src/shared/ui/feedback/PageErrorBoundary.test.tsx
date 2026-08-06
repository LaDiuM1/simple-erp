import { render, screen } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { afterEach, describe, expect, it, vi } from 'vitest';
import theme from '@/app/theme';
import PageErrorBoundary from './PageErrorBoundary';

function TestPage({ fails }: { fails: boolean }) {
  if (fails) {
    throw new Error('chunk load failed');
  }
  return <div>정상 화면</div>;
}

describe('PageErrorBoundary', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('화면 로딩 실패를 복구 가능한 오류 화면으로 바꾸고 route 변경 시 해제한다', () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const { rerender } = render(
      <ThemeProvider theme={theme}>
        <PageErrorBoundary key="/first">
          <TestPage fails />
        </PageErrorBoundary>
      </ThemeProvider>,
    );

    expect(screen.getByText(/화면을 불러오지 못했습니다/)).toBeVisible();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeVisible();

    rerender(
      <ThemeProvider theme={theme}>
        <PageErrorBoundary key="/second">
          <TestPage fails={false} />
        </PageErrorBoundary>
      </ThemeProvider>,
    );

    expect(screen.getByText('정상 화면')).toBeVisible();
  });
});
