import { api } from '@/shared/api/baseApi';
import type { DepartmentInfo, PositionInfo, RoleInfo, SupplierInfo } from '@/features/reference/types';

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
    getSuppliers: builder.query<SupplierInfo[], void>({
      query: () => ({ url: '/api/v1/suppliers', method: 'GET' }),
      providesTags: [{ type: 'Supplier', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
  useGetSuppliersQuery,
} = referenceApi;
