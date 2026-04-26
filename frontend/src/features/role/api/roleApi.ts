import { api } from '@/shared/api/baseApi';
import type { PageResponse } from '@/shared/types/api';
import type {
  RoleCreateRequest,
  RoleDetail,
  RoleSearchParams,
  RoleSummary,
  RoleUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const roleApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getRolesSummary: builder.query<PageResponse<RoleSummary>, RoleSearchParams>({
      query: (params) => ({
        url: '/api/v1/roles/summary',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Role', id: 'LIST' },
        ...(result?.content.map((m) => ({ type: 'Role' as const, id: m.id })) ?? []),
      ],
    }),
    getRole: builder.query<RoleDetail, number>({
      query: (id) => ({ url: `/api/v1/roles/${id}`, method: 'GET' }),
      providesTags: (_r, _e, id) => [{ type: 'Role', id }],
    }),
    createRole: builder.mutation<number, RoleCreateRequest>({
      query: (body) => ({ url: '/api/v1/roles', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Role', id: 'LIST' }],
    }),
    updateRole: builder.mutation<void, { id: number; body: RoleUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/roles/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_r, _e, { id }) => [
        { type: 'Role', id },
        { type: 'Role', id: 'LIST' },
      ],
    }),
    deleteRole: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/roles/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Role', id: 'LIST' }],
    }),
    checkRoleCodeAvailability: builder.query<{ available: boolean }, string>({
      query: (code) => ({
        url: '/api/v1/roles/code-availability',
        method: 'GET',
        params: { code },
      }),
    }),
  }),
});

export const {
  useGetRolesSummaryQuery,
  useGetRoleQuery,
  useCreateRoleMutation,
  useUpdateRoleMutation,
  useDeleteRoleMutation,
  useCheckRoleCodeAvailabilityQuery,
} = roleApi;
