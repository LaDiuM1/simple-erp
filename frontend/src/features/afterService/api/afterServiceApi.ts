import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, todayStamp, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';
import {
  DASHBOARD_CACHE_TAGS,
  DERIVED_CACHE_TAGS,
} from '@/shared/api/cacheDependencies';
import type { PageResponse } from '@/shared/types/api';
import type {
  AfterServiceCreateRequest,
  AfterServiceDetail,
  AfterServiceSearchParams,
  AfterServiceSummary,
  AfterServiceUpdateRequest,
  Engineer,
  EngineerRequest,
  ServiceExpenseRequest,
  ServiceVisitRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

export const afterServiceApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAfterServices: builder.query<PageResponse<AfterServiceSummary>, AfterServiceSearchParams>({
      query: (params) => ({
        url: '/api/v1/after-services',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'AfterService', id: 'LIST' },
        ...(result?.content.map((a) => ({ type: 'AfterService' as const, id: a.id })) ?? []),
      ],
    }),
    getAfterService: builder.query<AfterServiceDetail, number>({
      query: (id) => ({ url: `/api/v1/after-services/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'AfterService', id }],
    }),
    createAfterService: builder.mutation<number, AfterServiceCreateRequest>({
      query: (body) => ({ url: '/api/v1/after-services', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'AfterService', id: 'LIST' }, DASHBOARD_CACHE_TAGS.service],
    }),
    updateAfterService: builder.mutation<void, { id: number; body: AfterServiceUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/after-services/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'AfterService', id },
        { type: 'AfterService', id: 'LIST' },
        DASHBOARD_CACHE_TAGS.service,
      ],
    }),
    deleteAfterService: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/after-services/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'AfterService', id: 'LIST' }, DASHBOARD_CACHE_TAGS.service],
    }),
    deleteAfterServices: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/after-services', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'AfterService', id: 'LIST' }, DASHBOARD_CACHE_TAGS.service],
    }),

    // --- 방문 일지 (자식) — 상세 응답에 포함되므로 상세를 invalidate ---
    createServiceVisit: builder.mutation<number, { afterServiceId: number; body: ServiceVisitRequest }>({
      query: ({ afterServiceId, body }) => ({
        url: `/api/v1/after-services/${afterServiceId}/visits`,
        method: 'POST',
        data: body,
      }),
      invalidatesTags: (_result, _error, { afterServiceId }) => [
        { type: 'AfterService', id: afterServiceId },
      ],
    }),
    updateServiceVisit: builder.mutation<void, { id: number; afterServiceId: number; body: ServiceVisitRequest }>({
      query: ({ id, body }) => ({
        url: `/api/v1/after-services/visits/${id}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { afterServiceId }) => [
        { type: 'AfterService', id: afterServiceId },
      ],
    }),
    deleteServiceVisit: builder.mutation<void, { id: number; afterServiceId: number }>({
      query: ({ id }) => ({ url: `/api/v1/after-services/visits/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { afterServiceId }) => [
        { type: 'AfterService', id: afterServiceId },
      ],
    }),

    // --- 경비 (자식) — 목록의 경비 합계 컬럼도 갱신 필요 ---
    createServiceExpense: builder.mutation<number, { afterServiceId: number; body: ServiceExpenseRequest }>({
      query: ({ afterServiceId, body }) => ({
        url: `/api/v1/after-services/${afterServiceId}/expenses`,
        method: 'POST',
        data: body,
      }),
      invalidatesTags: (_result, _error, { afterServiceId }) => [
        { type: 'AfterService', id: afterServiceId },
        { type: 'AfterService', id: 'LIST' },
        DASHBOARD_CACHE_TAGS.service,
      ],
    }),
    updateServiceExpense: builder.mutation<void, { id: number; afterServiceId: number; body: ServiceExpenseRequest }>({
      query: ({ id, body }) => ({
        url: `/api/v1/after-services/expenses/${id}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { afterServiceId }) => [
        { type: 'AfterService', id: afterServiceId },
        { type: 'AfterService', id: 'LIST' },
        DASHBOARD_CACHE_TAGS.service,
      ],
    }),
    deleteServiceExpense: builder.mutation<void, { id: number; afterServiceId: number }>({
      query: ({ id }) => ({ url: `/api/v1/after-services/expenses/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { afterServiceId }) => [
        { type: 'AfterService', id: afterServiceId },
        { type: 'AfterService', id: 'LIST' },
        DASHBOARD_CACHE_TAGS.service,
      ],
    }),

    // --- 엔지니어 마스터 (서브 기능) ---
    getEngineers: builder.query<Engineer[], void>({
      query: () => ({ url: '/api/v1/after-services/engineers', method: 'GET' }),
      providesTags: [{ type: 'Engineer', id: 'LIST' }],
    }),
    createEngineer: builder.mutation<number, EngineerRequest>({
      query: (body) => ({ url: '/api/v1/after-services/engineers', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Engineer', id: 'LIST' }, DASHBOARD_CACHE_TAGS.service],
    }),
    updateEngineer: builder.mutation<void, { id: number; body: EngineerRequest }>({
      query: ({ id, body }) => ({
        url: `/api/v1/after-services/engineers/${id}`,
        method: 'PUT',
        data: body,
      }),
      // 이름 변경이 AS 목록 / 상세의 엔지니어 표기에 반영되도록 함께 invalidate.
      invalidatesTags: [
        { type: 'Engineer', id: 'LIST' },
        ...DERIVED_CACHE_TAGS.engineer,
        DASHBOARD_CACHE_TAGS.service,
      ],
    }),
    deleteEngineer: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/after-services/engineers/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Engineer', id: 'LIST' }, DASHBOARD_CACHE_TAGS.service],
    }),
  }),
});

export const {
  useGetAfterServicesQuery,
  useGetAfterServiceQuery,
  useCreateAfterServiceMutation,
  useUpdateAfterServiceMutation,
  useDeleteAfterServiceMutation,
  useDeleteAfterServicesMutation,
  useCreateServiceVisitMutation,
  useUpdateServiceVisitMutation,
  useDeleteServiceVisitMutation,
  useCreateServiceExpenseMutation,
  useUpdateServiceExpenseMutation,
  useDeleteServiceExpenseMutation,
  useGetEngineersQuery,
  useCreateEngineerMutation,
  useUpdateEngineerMutation,
  useDeleteEngineerMutation,
} = afterServiceApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 */
export function useDownloadAfterServicesExcel() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (params: Omit<AfterServiceSearchParams, 'page' | 'size'>) => {
      const response = await axiosInstance.get('/api/v1/after-services/excel', {
        params: cleanParams(params),
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });

      const filename = extractFilename(response.headers['content-disposition'])
        ?? `after-services_${todayStamp()}.xlsx`;

      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}
