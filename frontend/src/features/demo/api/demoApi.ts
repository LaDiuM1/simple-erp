import { api } from '@/shared/api/baseApi';
import type { DemoStatus } from '@/shared/demo/demoContract';
import { parseDemoStatus } from '@/features/demo/utils/parseDemoStatus';

export const demoApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDemoStatus: builder.query<DemoStatus, void>({
      query: () => ({ url: '/api/v1/demo/status', method: 'GET' }),
      transformResponse: parseDemoStatus,
      keepUnusedDataFor: 5,
    }),
  }),
});

export const { useGetDemoStatusQuery } = demoApi;
