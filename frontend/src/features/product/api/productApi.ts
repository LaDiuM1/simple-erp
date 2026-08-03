import { api } from '@/shared/api/baseApi';
import type { PageResponse } from '@/shared/types/api';
import type {
  ProductCategoryCreateRequest,
  ProductCategoryReference,
  ProductCategoryReorderRequest,
  ProductCategorySummary,
  ProductCategoryUpdateRequest,
  ProductCreateRequest,
  ProductDetail,
  ProductReference,
  ProductSearchParams,
  ProductSummary,
  ProductUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

export const productApi = api.injectEndpoints({
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
    getProductReferences: builder.query<PageResponse<ProductReference>, ProductSearchParams>({
      query: (params) => ({
        url: '/api/v1/products/reference',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Product', id: 'REFERENCE_LIST' },
        ...(result?.content.map((product) => ({
          type: 'Product' as const,
          id: product.id,
        })) ?? []),
      ],
    }),
    getProductReference: builder.query<ProductReference, number>({
      query: (id) => ({ url: `/api/v1/products/reference/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Product', id }],
    }),
    getProductReferenceCategories: builder.query<ProductCategoryReference[], void>({
      query: () => ({ url: '/api/v1/products/reference/categories', method: 'GET' }),
      providesTags: [{ type: 'ProductCategory', id: 'REFERENCE_LIST' }],
    }),
    createProduct: builder.mutation<number, ProductCreateRequest>({
      query: (body) => ({ url: '/api/v1/products', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Product', id: 'LIST' },
        { type: 'Product', id: 'REFERENCE_LIST' },
      ],
    }),
    updateProduct: builder.mutation<void, { id: number; body: ProductUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/products/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Product', id },
        { type: 'Product', id: 'LIST' },
        { type: 'Product', id: 'REFERENCE_LIST' },
      ],
    }),
    deleteProduct: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/products/${id}`, method: 'DELETE' }),
      invalidatesTags: [
        { type: 'Product', id: 'LIST' },
        { type: 'Product', id: 'REFERENCE_LIST' },
      ],
    }),
    deleteProducts: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/products', method: 'DELETE', data: ids }),
      invalidatesTags: [
        { type: 'Product', id: 'LIST' },
        { type: 'Product', id: 'REFERENCE_LIST' },
      ],
    }),
    getProductCategories: builder.query<ProductCategorySummary[], void>({
      query: () => ({ url: '/api/v1/products/categories', method: 'GET' }),
      providesTags: [{ type: 'ProductCategory', id: 'LIST' }],
    }),
    createProductCategory: builder.mutation<number, ProductCategoryCreateRequest>({
      query: (body) => ({ url: '/api/v1/products/categories', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'ProductCategory', id: 'LIST' }],
    }),
    /** 이름 변경은 제품 목록의 카테고리명 표시에도 반영돼야 하므로 Product LIST 도 무효화. */
    updateProductCategory: builder.mutation<void, { id: number; body: ProductCategoryUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/products/categories/${id}`, method: 'PUT', data: body }),
      invalidatesTags: [
        { type: 'ProductCategory', id: 'LIST' },
        { type: 'ProductCategory', id: 'REFERENCE_LIST' },
        { type: 'Product', id: 'LIST' },
        { type: 'Product', id: 'REFERENCE_LIST' },
      ],
    }),
    deleteProductCategory: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/products/categories/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'ProductCategory', id: 'LIST' }],
    }),
    reorderProductCategories: builder.mutation<void, ProductCategoryReorderRequest>({
      query: (body) => ({ url: '/api/v1/products/categories/reorder', method: 'PUT', data: body }),
      invalidatesTags: [{ type: 'ProductCategory', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetProductsSummaryQuery,
  useGetProductQuery,
  useGetProductReferencesQuery,
  useGetProductReferenceQuery,
  useGetProductReferenceCategoriesQuery,
  useCreateProductMutation,
  useUpdateProductMutation,
  useDeleteProductMutation,
  useDeleteProductsMutation,
  useGetProductCategoriesQuery,
  useCreateProductCategoryMutation,
  useUpdateProductCategoryMutation,
  useDeleteProductCategoryMutation,
  useReorderProductCategoriesMutation,
} = productApi;
