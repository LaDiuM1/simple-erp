import { api } from '@/shared/api/baseApi';
import type { DashboardSummary } from '../types';

const dashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardSummary: builder.query<DashboardSummary, void>({
      query: () => ({ url: '/api/v1/dashboard/summary', method: 'GET' }),
    }),
  }),
});

export const { useGetDashboardSummaryQuery } = dashboardApi;
