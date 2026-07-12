import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, todayStamp, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';
import type { PageResponse } from '@/shared/types/api';
import type {
  ContractCreateRequest,
  ContractDetail,
  ContractPaymentRequest,
  ContractSearchParams,
  ContractSummary,
  ContractUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const contractApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getContracts: builder.query<PageResponse<ContractSummary>, ContractSearchParams>({
      query: (params) => ({
        url: '/api/v1/contracts',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Contract', id: 'LIST' },
        ...(result?.content.map((c) => ({ type: 'Contract' as const, id: c.id })) ?? []),
      ],
    }),
    getContract: builder.query<ContractDetail, number>({
      query: (id) => ({ url: `/api/v1/contracts/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Contract', id }],
    }),
    createContract: builder.mutation<number, ContractCreateRequest>({
      query: (body) => ({ url: '/api/v1/contracts', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Contract', id: 'LIST' }],
    }),
    updateContract: builder.mutation<void, { id: number; body: ContractUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/contracts/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Contract', id },
        { type: 'Contract', id: 'LIST' },
      ],
    }),
    deleteContract: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/contracts/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Contract', id: 'LIST' }],
    }),
    deleteContracts: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/contracts', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'Contract', id: 'LIST' }],
    }),

    // --- 대금 스케줄 (자식) — 상세 응답에 포함되므로 상세 + 목록 (미수금 컬럼) 을 invalidate ---
    createContractPayment: builder.mutation<number, { contractId: number; body: ContractPaymentRequest }>({
      query: ({ contractId, body }) => ({
        url: `/api/v1/contracts/${contractId}/payments`,
        method: 'POST',
        data: body,
      }),
      invalidatesTags: (_result, _error, { contractId }) => [
        { type: 'Contract', id: contractId },
        { type: 'Contract', id: 'LIST' },
      ],
    }),
    updateContractPayment: builder.mutation<void, { id: number; contractId: number; body: ContractPaymentRequest }>({
      query: ({ id, body }) => ({
        url: `/api/v1/contracts/payments/${id}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { contractId }) => [
        { type: 'Contract', id: contractId },
        { type: 'Contract', id: 'LIST' },
      ],
    }),
    deleteContractPayment: builder.mutation<void, { id: number; contractId: number }>({
      query: ({ id }) => ({ url: `/api/v1/contracts/payments/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { contractId }) => [
        { type: 'Contract', id: contractId },
        { type: 'Contract', id: 'LIST' },
      ],
    }),

    // --- 변경 이력 메모 (자식) ---
    createContractNote: builder.mutation<number, { contractId: number; content: string }>({
      query: ({ contractId, content }) => ({
        url: `/api/v1/contracts/${contractId}/notes`,
        method: 'POST',
        data: { content },
      }),
      invalidatesTags: (_result, _error, { contractId }) => [{ type: 'Contract', id: contractId }],
    }),
    deleteContractNote: builder.mutation<void, { id: number; contractId: number }>({
      query: ({ id }) => ({ url: `/api/v1/contracts/notes/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { contractId }) => [{ type: 'Contract', id: contractId }],
    }),
  }),
});

export const {
  useGetContractsQuery,
  useGetContractQuery,
  useCreateContractMutation,
  useUpdateContractMutation,
  useDeleteContractMutation,
  useDeleteContractsMutation,
  useCreateContractPaymentMutation,
  useUpdateContractPaymentMutation,
  useDeleteContractPaymentMutation,
  useCreateContractNoteMutation,
  useDeleteContractNoteMutation,
} = contractApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 */
export function useDownloadContractsExcel() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (params: Omit<ContractSearchParams, 'page' | 'size'>) => {
      const response = await axiosInstance.get('/api/v1/contracts/excel', {
        params: cleanParams(params),
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });

      const filename = extractFilename(response.headers['content-disposition'])
        ?? `contracts_${todayStamp()}.xlsx`;

      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}
