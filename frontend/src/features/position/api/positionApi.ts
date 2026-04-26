import { api } from '@/shared/api/baseApi';
import type { PageResponse } from '@/shared/types/api';
import type {
  PositionCreateRequest,
  PositionDetail,
  PositionSearchParams,
  PositionSummary,
  PositionUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const positionApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPositionsSummary: builder.query<PageResponse<PositionSummary>, PositionSearchParams>({
      query: (params) => ({
        url: '/api/v1/positions/summary',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Position', id: 'LIST' },
        ...(result?.content.map((m) => ({ type: 'Position' as const, id: m.id })) ?? []),
      ],
    }),
    getPosition: builder.query<PositionDetail, number>({
      query: (id) => ({ url: `/api/v1/positions/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Position', id }],
    }),
    createPosition: builder.mutation<number, PositionCreateRequest>({
      query: (body) => ({ url: '/api/v1/positions', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Position', id: 'LIST' }],
    }),
    updatePosition: builder.mutation<void, { id: number; body: PositionUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/positions/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Position', id },
        { type: 'Position', id: 'LIST' },
      ],
    }),
    deletePosition: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/positions/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Position', id: 'LIST' }],
    }),
    checkPositionCodeAvailability: builder.query<{ available: boolean }, string>({
      query: (code) => ({
        url: '/api/v1/positions/code-availability',
        method: 'GET',
        params: { code },
      }),
    }),
  }),
});

export const {
  useGetPositionsSummaryQuery,
  useGetPositionQuery,
  useCreatePositionMutation,
  useUpdatePositionMutation,
  useDeletePositionMutation,
  useCheckPositionCodeAvailabilityQuery,
} = positionApi;
