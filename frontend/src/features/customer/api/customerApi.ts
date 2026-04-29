import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, todayStamp, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';
import type { PageResponse } from '@/shared/types/api';
import type { ExcelUploadResult } from '@/shared/ui/ExcelUpload';
import type {
  CustomerCreateRequest,
  CustomerDetail,
  CustomerSearchParams,
  CustomerSummary,
  CustomerUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const customerApi = api.injectEndpoints({
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
    createCustomer: builder.mutation<number, CustomerCreateRequest>({
      query: (body) => ({ url: '/api/v1/customers', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),
    updateCustomer: builder.mutation<void, { id: number; body: CustomerUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/customers/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Customer', id },
        { type: 'Customer', id: 'LIST' },
      ],
    }),
    deleteCustomer: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/customers/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),
    deleteCustomers: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/customers', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
    }),
    uploadCustomersExcel: builder.mutation<ExcelUploadResult, FormData>({
      query: (form) => ({ url: '/api/v1/customers/excel/upload', method: 'POST', data: form }),
      invalidatesTags: [{ type: 'Customer', id: 'LIST' }],
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
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (params: Omit<CustomerSearchParams, 'page' | 'size'>) => {
      const response = await axiosInstance.get('/api/v1/customers/excel', {
        params: cleanParams(params),
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });

      const filename = extractFilename(response.headers['content-disposition'])
        ?? `customers_${todayStamp()}.xlsx`;

      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}

/**
 * 업로드 양식 (.xlsx) 다운로드 — 다운로드와 동일한 헤더 / 폭 / 톤. binary 응답이라 axios 직접 호출.
 */
export function useDownloadCustomersTemplate() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(async () => {
    const response = await axiosInstance.get('/api/v1/customers/excel/template', {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const filename = extractFilename(response.headers['content-disposition'])
      ?? 'customers_template.xlsx';
    triggerBrowserDownload(response.data, filename);
  }, [token]);
}
