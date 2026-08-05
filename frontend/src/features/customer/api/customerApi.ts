import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import { DERIVED_CACHE_TAGS } from '@/shared/api/cacheDependencies';
import { todayStamp } from '@/shared/api/excelDownload';
import { useBlobDownload } from '@/shared/api/useBlobDownload';
import type { PageResponse } from '@/shared/types/api';
import type { ExcelUploadResult } from '@/shared/ui/ExcelUpload';
import type {
  CustomerCreateRequest,
  CustomerDetail,
  CustomerReference,
  SalesCustomerReference,
  CustomerSearchParams,
  CustomerSummary,
  CustomerUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

export const customerApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCustomers: builder.query<PageResponse<CustomerSummary>, CustomerSearchParams>({
      query: (params) => ({
        url: '/api/v1/customers',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Customer', id: 'LIST' },
        ...(result?.content.map((c) => ({ type: 'Customer' as const, id: c.id })) ?? []),
      ],
    }),
    getCustomer: builder.query<CustomerDetail, number>({
      query: (id) => ({ url: `/api/v1/customers/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Customer', id }],
    }),
    getCustomerReferences: builder.query<PageResponse<CustomerReference>, CustomerSearchParams>({
      query: (params) => ({
        url: '/api/v1/customers/reference',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Customer', id: 'REFERENCE_LIST' },
        ...(result?.content.map((c) => ({ type: 'Customer' as const, id: c.id })) ?? []),
      ],
    }),
    getSalesCustomerReferences: builder.query<PageResponse<SalesCustomerReference>, CustomerSearchParams>({
      query: (params) => ({
        url: '/api/v1/customers/sales-reference',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Customer', id: 'SALES_REFERENCE_LIST' },
        ...(result?.content.map((c) => ({ type: 'Customer' as const, id: c.id })) ?? []),
      ],
    }),
    getSalesCustomerReference: builder.query<SalesCustomerReference, number>({
      query: (id) => ({ url: `/api/v1/customers/sales-reference/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Customer', id }],
    }),
    createCustomer: builder.mutation<number, CustomerCreateRequest>({
      query: (body) => ({ url: '/api/v1/customers', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Customer', id: 'LIST' },
        { type: 'Customer', id: 'REFERENCE_LIST' },
        { type: 'Customer', id: 'SALES_REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.customer,
      ],
    }),
    updateCustomer: builder.mutation<void, { id: number; body: CustomerUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/customers/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
        { type: 'Customer', id: 'REFERENCE_LIST' },
        { type: 'Customer', id: 'SALES_REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.customer,
      ],
    }),
    deleteCustomer: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/customers/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
        { type: 'Customer', id: 'REFERENCE_LIST' },
        { type: 'Customer', id: 'SALES_REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.customer,
      ],
    }),
    deleteCustomers: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/customers', method: 'DELETE', data: ids }),
      invalidatesTags: (_result, _error, ids) => [
        { type: 'Customer', id: 'LIST' },
        { type: 'Customer', id: 'REFERENCE_LIST' },
        { type: 'Customer', id: 'SALES_REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.customer,
        ...ids.map((id) => ({ type: 'Customer' as const, id })),
      ],
    }),
    uploadCustomersExcel: builder.mutation<ExcelUploadResult, FormData>({
      query: (form) => ({ url: '/api/v1/customers/excel/upload', method: 'POST', data: form }),
      invalidatesTags: [
        { type: 'Customer', id: 'LIST' },
        { type: 'Customer', id: 'REFERENCE_LIST' },
        { type: 'Customer', id: 'SALES_REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.customer,
      ],
    }),
    checkCustomerCodeAvailability: builder.query<{ available: boolean }, string>({
      query: (code) => ({
        url: '/api/v1/customers/code-availability',
        method: 'GET',
        params: { code },
      }),
    }),
    checkCustomerBizRegNoAvailability: builder.query<{ available: boolean }, string>({
      query: (bizRegNo) => ({
        url: '/api/v1/customers/bizregno-availability',
        method: 'GET',
        params: { bizRegNo },
      }),
    }),
  }),
});

export const {
  useGetCustomersQuery,
  useGetCustomerQuery,
  useGetCustomerReferencesQuery,
  useGetSalesCustomerReferencesQuery,
  useGetSalesCustomerReferenceQuery,
  useCreateCustomerMutation,
  useUpdateCustomerMutation,
  useDeleteCustomerMutation,
  useDeleteCustomersMutation,
  useUploadCustomersExcelMutation,
  useCheckCustomerCodeAvailabilityQuery,
  useCheckCustomerBizRegNoAvailabilityQuery,
} = customerApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 */
export function useDownloadCustomersExcel() {
  const download = useBlobDownload();

  return useCallback(
    (params: Omit<CustomerSearchParams, 'page' | 'size'>) => download({
      url: '/api/v1/customers/excel',
      fallbackName: `customers_${todayStamp()}.xlsx`,
      params: cleanParams(params),
    }),
    [download],
  );
}

/**
 * 업로드 양식 (.xlsx) 다운로드 — 다운로드와 동일한 헤더 / 폭 / 톤. binary 응답이라 axios 직접 호출.
 */
export function useDownloadCustomersTemplate() {
  const download = useBlobDownload();

  return useCallback(async () => {
    await download({
      url: '/api/v1/customers/excel/template',
      fallbackName: 'customers_template.xlsx',
    });
  }, [download]);
}
