import { api } from '@/shared/api/baseApi';
import { DASHBOARD_CACHE_TAGS } from '@/shared/api/cacheDependencies';
import type {
  DashboardSales,
  DashboardServiceStats,
  DashboardSummary,
  ExpiringWarrantyItem,
} from '../types';

export const dashboardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDashboardSummary: builder.query<DashboardSummary, void>({
      query: () => ({ url: '/api/v1/dashboard/summary', method: 'GET' }),
      providesTags: [DASHBOARD_CACHE_TAGS.summary],
    }),
    getDashboardSales: builder.query<DashboardSales, void>({
      query: () => ({ url: '/api/v1/dashboard/sales', method: 'GET' }),
      providesTags: [DASHBOARD_CACHE_TAGS.sales],
    }),
    getDashboardServiceStats: builder.query<DashboardServiceStats, void>({
      query: () => ({ url: '/api/v1/dashboard/service', method: 'GET' }),
      providesTags: [DASHBOARD_CACHE_TAGS.service],
    }),
    getDashboardWarranty: builder.query<ExpiringWarrantyItem[], void>({
      query: () => ({ url: '/api/v1/dashboard/warranty', method: 'GET' }),
      providesTags: [DASHBOARD_CACHE_TAGS.warranty],
    }),
  }),
});

export const {
  useGetDashboardSummaryQuery,
  useGetDashboardSalesQuery,
  useGetDashboardServiceStatsQuery,
  useGetDashboardWarrantyQuery,
} = dashboardApi;
