import { api } from '@/shared/api/baseApi';
import type { DepartmentInfo, PositionInfo, RoleInfo } from '@/features/reference/types';

const referenceApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getDepartments: builder.query<DepartmentInfo[], void>({
      query: () => ({ url: '/api/v1/departments', method: 'GET' }),
      providesTags: [{ type: 'Department', id: 'LIST' }],
    }),
    getPositions: builder.query<PositionInfo[], void>({
      query: () => ({ url: '/api/v1/positions', method: 'GET' }),
      providesTags: [{ type: 'Position', id: 'LIST' }],
    }),
    getRoles: builder.query<RoleInfo[], void>({
      query: () => ({ url: '/api/v1/roles', method: 'GET' }),
      providesTags: [{ type: 'Role', id: 'LIST' }],
    }),
  }),
});

export const { useGetDepartmentsQuery, useGetPositionsQuery, useGetRolesQuery } = referenceApi;
