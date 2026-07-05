import { http, HttpResponse } from 'msw';
import type { ApiResponse } from '@/shared/types/api';
import type { DashboardSummary } from '@/features/dashboard/types';

/** BE ApiResponse 래핑 규격 그대로 목 응답 생성. */
export function apiResponse<T>(data: T): ApiResponse<T> {
  return { status: 200, message: 'OK', data };
}

export const mockDashboardSummary: DashboardSummary = {
  kpi: {
    totalCustomers: 312,
    totalSalesContacts: 5214,
    activeEmployees: 28,
    monthlySalesActivities: 47,
  },
  recentCustomers: [],
  recentActivities: [],
  weeklyActivityTrend: [
    { weekStart: '2026-06-22', count: 44 },
    { weekStart: '2026-06-29', count: 47 },
  ],
  followUps: [],
  newCustomersThisWeek: 6,
  newSalesContactsThisWeek: 31,
  newSalesContactsThisMonth: 128,
  uncontactedCustomersThisMonth: 23,
};

export const handlers = [
  http.get('*/api/v1/dashboard/summary', () =>
    HttpResponse.json(apiResponse(mockDashboardSummary)),
  ),
];
