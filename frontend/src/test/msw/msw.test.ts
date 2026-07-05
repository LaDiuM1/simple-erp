import { describe, expect, it } from 'vitest';
import type { ApiResponse } from '@/shared/types/api';
import type { DashboardSummary } from '@/features/dashboard/types';

describe('msw 인프라 스모크', () => {
  it('핸들러가 ApiResponse 래핑 규격으로 응답', async () => {
    const res = await fetch('http://localhost/api/v1/dashboard/summary');
    const body = (await res.json()) as ApiResponse<DashboardSummary>;

    expect(res.status).toBe(200);
    expect(body.status).toBe(200);
    expect(body.data.kpi.totalCustomers).toBe(312);
  });
});
