import { api } from '@/shared/api/baseApi';
import { DERIVED_CACHE_TAGS } from '@/shared/api/cacheDependencies';
import type { PageResponse } from '@/shared/types/api';
import type {
  SupplierCreateRequest,
  SupplierDetail,
  SupplierSearchParams,
  SupplierSummary,
  SupplierUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

export const supplierApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSuppliersSummary: builder.query<PageResponse<SupplierSummary>, SupplierSearchParams>({
      query: (params) => ({
        url: '/api/v1/suppliers/summary',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Supplier', id: 'LIST' },
        ...(result?.content.map((m) => ({ type: 'Supplier' as const, id: m.id })) ?? []),
      ],
    }),
    getSupplier: builder.query<SupplierDetail, number>({
      query: (id) => ({ url: `/api/v1/suppliers/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Supplier', id }],
    }),
    createSupplier: builder.mutation<number, SupplierCreateRequest>({
      query: (body) => ({ url: '/api/v1/suppliers', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Supplier', id: 'LIST' }, ...DERIVED_CACHE_TAGS.supplier],
    }),
    updateSupplier: builder.mutation<void, { id: number; body: SupplierUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/suppliers/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Supplier', id },
        { type: 'Supplier', id: 'LIST' },
        ...DERIVED_CACHE_TAGS.supplier,
      ],
    }),
    deleteSupplier: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/suppliers/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Supplier', id: 'LIST' }, ...DERIVED_CACHE_TAGS.supplier],
    }),
    deleteSuppliers: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/suppliers', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'Supplier', id: 'LIST' }, ...DERIVED_CACHE_TAGS.supplier],
    }),
  }),
});

export const {
  useGetSuppliersSummaryQuery,
  useGetSupplierQuery,
  useCreateSupplierMutation,
  useUpdateSupplierMutation,
  useDeleteSupplierMutation,
  useDeleteSuppliersMutation,
} = supplierApi;
