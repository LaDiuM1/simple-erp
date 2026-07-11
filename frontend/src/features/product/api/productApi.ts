import { api } from '@/shared/api/baseApi';
import type { PageResponse } from '@/shared/types/api';
import type {
  ProductCreateRequest,
  ProductDetail,
  ProductSearchParams,
  ProductSummary,
  ProductUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const productApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getProductsSummary: builder.query<PageResponse<ProductSummary>, ProductSearchParams>({
      query: (params) => ({
        url: '/api/v1/products/summary',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Product', id: 'LIST' },
        ...(result?.content.map((m) => ({ type: 'Product' as const, id: m.id })) ?? []),
      ],
    }),
    getProduct: builder.query<ProductDetail, number>({
      query: (id) => ({ url: `/api/v1/products/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Product', id }],
    }),
    createProduct: builder.mutation<number, ProductCreateRequest>({
      query: (body) => ({ url: '/api/v1/products', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Product', id: 'LIST' }],
    }),
    updateProduct: builder.mutation<void, { id: number; body: ProductUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/products/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Product', id },
        { type: 'Product', id: 'LIST' },
      ],
    }),
    deleteProduct: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/products/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Product', id: 'LIST' }],
    }),
    deleteProducts: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/products', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'Product', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetProductsSummaryQuery,
  useGetProductQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  useDeleteProductsMutation,
} = productApi;
