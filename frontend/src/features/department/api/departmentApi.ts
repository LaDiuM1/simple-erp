import { api } from '@/shared/api/baseApi';
import type { PageResponse } from '@/shared/types/api';
import type {
  DepartmentCreateRequest,
  DepartmentDetail,
  DepartmentSearchParams,
  DepartmentSummary,
  DepartmentUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const departmentApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDepartmentsSummary: builder.query<PageResponse<DepartmentSummary>, DepartmentSearchParams>({
      query: (params) => ({
        url: '/api/v1/departments/summary',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Department', id: 'LIST' },
        ...(result?.content.map((m) => ({ type: 'Department' as const, id: m.id })) ?? []),
      ],
    }),
    getDepartment: builder.query<DepartmentDetail, number>({
      query: (id) => ({ url: `/api/v1/departments/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Department', id }],
    }),
    createDepartment: builder.mutation<number, DepartmentCreateRequest>({
      query: (body) => ({ url: '/api/v1/departments', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Department', id: 'LIST' }],
    }),
    updateDepartment: builder.mutation<void, { id: number; body: DepartmentUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/departments/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Department', id },
        { type: 'Department', id: 'LIST' },
      ],
    }),
    deleteDepartment: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/departments/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Department', id: 'LIST' }],
    }),
    deleteDepartments: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/departments', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'Department', id: 'LIST' }],
    }),
    checkDepartmentCodeAvailability: builder.query<{ available: boolean }, string>({
      query: (code) => ({
        url: '/api/v1/departments/code-availability',
        method: 'GET',
        params: { code },
      }),
    }),
  }),
});

export const {
  useGetDepartmentsSummaryQuery,
  useGetDepartmentQuery,
  useCreateDepartmentMutation,
  useUpdateDepartmentMutation,
  useDeleteDepartmentMutation,
  useDeleteDepartmentsMutation,
  useCheckDepartmentCodeAvailabilityQuery,
} = departmentApi;
