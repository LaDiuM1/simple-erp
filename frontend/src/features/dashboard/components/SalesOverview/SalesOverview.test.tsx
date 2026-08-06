import { describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { renderWithTheme } from '@/test/renderWithTheme';
import type { DashboardSales } from '../../types';
import SalesOverview from './SalesOverview';

describe('SalesOverview', () => {
  it('계약 건수가 0인 월도 6개월 추이의 한 달로 유지한다', () => {
    const data: DashboardSales = {
      monthlyStats: [
        { month: '2026-07', count: 0, totalAmount: 0 },
        { month: '2026-08', count: 0, totalAmount: 0 },
      ],
      outstanding: {
        totalFinalAmount: 0,
        totalPaidAmount: 0,
        totalOutstandingAmount: 0,
      },
    };

    renderWithTheme(
      <MemoryRouter>
        <SalesOverview data={data} />
      </MemoryRouter>,
    );

    expect(screen.getByText('26년 7월')).toBeInTheDocument();
    expect(screen.getByText('26년 8월')).toBeInTheDocument();
    expect(screen.queryByText('최근 6개월 계약 실적이 없습니다.')).not.toBeInTheDocument();
    expect(screen.getByRole('progressbar', { name: '수금 진행률' })).toHaveAttribute(
      'aria-valuenow',
      '0',
    );
  });
});
