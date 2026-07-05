import { describe, expect, it, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { renderWithTheme } from '@/test/renderWithTheme';
import StatTile from './StatTile';

describe('StatTile', () => {
  it('라벨 / 값 / 증감 렌더', () => {
    renderWithTheme(
      <StatTile
        label="총 고객사"
        value={312}
        unit="개사"
        delta={{ value: 6, periodLabel: '이번 주' }}
        onClick={() => {}}
      />,
    );

    expect(screen.getByText('총 고객사')).toBeInTheDocument();
    expect(screen.getByText('312')).toBeInTheDocument();
    expect(screen.getByText(/이번 주/)).toBeInTheDocument();
  });

  it('delta 가 0 이하이면 증감 미노출', () => {
    renderWithTheme(
      <StatTile
        label="재직 직원"
        value={28}
        unit="명"
        delta={{ value: 0, periodLabel: '이번 주' }}
        onClick={() => {}}
      />,
    );

    expect(screen.queryByText(/이번 주/)).not.toBeInTheDocument();
  });

  it('클릭 시 onClick 호출', async () => {
    const onClick = vi.fn();
    renderWithTheme(
      <StatTile label="영업 명부" value={5214} unit="명" onClick={onClick} />,
    );

    await userEvent.click(screen.getByRole('button'));
    expect(onClick).toHaveBeenCalledTimes(1);
  });
});
