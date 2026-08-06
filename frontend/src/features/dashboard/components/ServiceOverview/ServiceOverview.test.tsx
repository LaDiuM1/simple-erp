import { screen, within } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { renderWithTheme } from '@/test/renderWithTheme';
import type { DashboardServiceStats } from '../../types';
import ServiceOverview from './ServiceOverview';

describe('ServiceOverview', () => {
  it('유형별 건수와 처리 비용을 함께 제공한다', () => {
    const data: DashboardServiceStats = {
      typeStats: [
        { type: 'REPAIR', typeLabel: '수리', count: 4, expenseTotal: 1_200_000 },
      ],
      engineerStats: [],
    };

    renderWithTheme(
      <MemoryRouter>
        <ServiceOverview data={data} />
      </MemoryRouter>,
    );

    const summary = screen.getByLabelText('AS 운영 요약');
    expect(within(summary).getByText('4건')).toBeVisible();
    expect(within(summary).getByText('1,200,000원')).toBeVisible();

    const repairRow = screen.getByLabelText('수리 AS 현황');
    expect(within(repairRow).getByText('4건')).toBeVisible();
    expect(within(repairRow).getByText('1,200,000원')).toBeVisible();
  });
});
