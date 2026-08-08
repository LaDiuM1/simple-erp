import { act, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import type { EmployeeProfileResponse } from '@/features/employee/types';
import HeroBanner from './HeroBanner';

const profile: EmployeeProfileResponse = {
  id: 1,
  loginId: 'demo.manager',
  name: '김서현',
  departmentName: '경영지원팀',
  positionName: '팀장',
  roleName: '관리자',
  roleCode: 'MANAGER',
  menuPermissions: [],
};

describe('HeroBanner', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 7, 12, 23, 59, 59));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('자정이 지나면 날짜만 다음 날로 갱신한다', () => {
    renderWithTheme(<HeroBanner profile={profile} />);
    expect(screen.getByText(/2026년 8월 12일/)).toBeInTheDocument();

    act(() => vi.advanceTimersByTime(1_000));

    expect(screen.getByText(/2026년 8월 13일/)).toBeInTheDocument();
  });
});
