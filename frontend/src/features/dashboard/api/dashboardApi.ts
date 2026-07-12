import { api } from '@/shared/api/baseApi';
import type {
  DashboardSales,
  DashboardServiceStats,
  DashboardSummary,
  ExpiringWarrantyItem,
} from '../types';

const dashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardSummary: builder.query<DashboardSummary, void>({
      query: () => ({ url: '/api/v1/dashboard/summary', method: 'GET' }),
    }),
    getDashboardSales: builder.query<DashboardSales, void>({
      query: () => ({ url: '/api/v1/dashboard/sales', method: 'GET' }),
    }),
    getDashboardServiceStats: builder.query<DashboardServiceStats, void>({
      query: () => ({ url: '/api/v1/dashboard/service', method: 'GET' }),
    }),
    getDashboardWarranty: builder.query<ExpiringWarrantyItem[], void>({
      query: () => ({ url: '/api/v1/dashboard/warranty', method: 'GET' }),
    }),
  }),
});

export const {
  useGetDashboardSummaryQuery,
  useGetDashboardSalesQuery,
  useGetDashboardServiceStatsQuery,
  useGetDashboardWarrantyQuery,
} = dashboardApi;
