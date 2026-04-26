import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { useAppSelector } from '@/app/hooks';
import type { PageResponse } from '@/shared/types/api';
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

function triggerBrowserDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function extractFilename(contentDisposition: string | undefined): string | null {
  if (!contentDisposition) return null;
  const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
  if (!match) return null;
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
}

function todayStamp(): string {
  const d = new Date();
  return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
}
