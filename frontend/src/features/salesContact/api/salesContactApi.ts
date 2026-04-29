import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, todayStamp, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';
import type { PageResponse } from '@/shared/types/api';
import type { ExcelUploadResult } from '@/shared/ui/ExcelUpload';
import type {
  SalesContactCreateRequest,
  SalesContactDetail,
  SalesContactEmployment,
  SalesContactEmploymentCreateRequest,
  SalesContactEmploymentTerminateRequest,
  SalesContactEmploymentUpdateRequest,
  SalesContactListFilters,
  SalesContactSearchParams,
  SalesContactSummary,
  SalesContactUpdateRequest,
} from '../types';

/**
 * undefined / null / 빈 문자열 / 빈 배열 제거. 배열은 그대로 보존하여 axios paramsSerializer 가
 * `?key=v1&key=v2` 형태로 직렬화 (Spring `@RequestParam List<Long>` 와 호환).
 */
function cleanParams(params: object): Record<string, unknown> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => {
      if (v === undefined || v === null || v === '') return false;
      if (Array.isArray(v) && v.length === 0) return false;
      return true;
    }),
  );
}

/** 배열은 `key=v1&key=v2` 로, 그 외는 기본 직렬화. */
const paramsSerializer = {
  indexes: null as null | boolean,
};

const salesContactApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSalesContacts: builder.query<PageResponse<SalesContactSummary>, SalesContactSearchParams>({
      query: (params) => ({
        url: '/api/v1/sales-contacts',
        method: 'GET',
        params: cleanParams(params),
        paramsSerializer,
      }),
      providesTags: (result) => [
        { type: 'SalesContact', id: 'LIST' },
        ...(result?.content.map((c) => ({ type: 'SalesContact' as const, id: c.id })) ?? []),
      ],
    }),
    getSalesContact: builder.query<SalesContactDetail, number>({
      query: (id) => ({ url: `/api/v1/sales-contacts/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [
        { type: 'SalesContact', id },
        { type: 'SalesContactEmployment', id: `CONTACT:${id}` },
      ],
    }),
    createSalesContact: builder.mutation<number, SalesContactCreateRequest>({
      query: (body) => ({ url: '/api/v1/sales-contacts', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'SalesContact', id: 'LIST' }],
    }),
    updateSalesContact: builder.mutation<void, { id: number; body: SalesContactUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/sales-contacts/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'SalesContact', id },
        { type: 'SalesContact', id: 'LIST' },
      ],
    }),
    deleteSalesContact: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/sales-contacts/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'SalesContact', id: 'LIST' }],
    }),
    deleteSalesContacts: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/sales-contacts', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'SalesContact', id: 'LIST' }],
    }),
    uploadSalesContactsExcel: builder.mutation<ExcelUploadResult, FormData>({
      query: (form) => ({ url: '/api/v1/sales-contacts/excel/upload', method: 'POST', data: form }),
      invalidatesTags: [{ type: 'SalesContact', id: 'LIST' }],
    }),

    /**
     * 휴대폰 번호 사용 가능 여부 — 디바운스된 입력값 기반 호출.
     * excludeId 가 있으면 본인 자신은 제외 (수정 모드 본인 번호 그대로 두기 허용).
     */
    checkSalesContactMobilePhoneAvailability: builder.query<
      { available: boolean },
      { mobilePhone: string; excludeId?: number }
    >({
      query: ({ mobilePhone, excludeId }) => ({
        url: '/api/v1/sales-contacts/mobile-phone-availability',
        method: 'GET',
        params: excludeId == null ? { mobilePhone } : { mobilePhone, excludeId },
      }),
    }),

    /** 고객사별 재직 명부 — 고객사 영업 상세 페이지에서 호출. */
    getSalesContactEmploymentsByCustomerId: builder.query<SalesContactEmployment[], number>({
      query: (customerId) => ({
        url: '/api/v1/sales-contacts/employments',
        method: 'GET',
        params: { customerId },
      }),
      providesTags: (_result, _error, customerId) => [
        { type: 'SalesContactEmployment', id: `CUSTOMER:${customerId}` },
      ],
    }),

    createSalesContactEmployment: builder.mutation<
      number,
      { contactId: number; body: SalesContactEmploymentCreateRequest }
    >({
      query: ({ contactId, body }) => ({
        url: `/api/v1/sales-contacts/${contactId}/employments`,
        method: 'POST',
        data: body,
      }),
      invalidatesTags: (_result, _error, { contactId, body }) => [
        { type: 'SalesContactEmployment', id: `CONTACT:${contactId}` },
        ...(body.customerId
          ? [{ type: 'SalesContactEmployment' as const, id: `CUSTOMER:${body.customerId}` }]
          : []),
      ],
    }),
    updateSalesContactEmployment: builder.mutation<
      void,
      { id: number; contactId: number; customerId: number | null; body: SalesContactEmploymentUpdateRequest }
    >({
      query: ({ id, body }) => ({
        url: `/api/v1/sales-contacts/employments/${id}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { contactId, customerId, body }) => {
        const tags = [
          { type: 'SalesContactEmployment' as const, id: `CONTACT:${contactId}` },
        ];
        // 변경 전후의 customer 모두 invalidate (회사 이전 케이스 대비)
        if (customerId) tags.push({ type: 'SalesContactEmployment' as const, id: `CUSTOMER:${customerId}` });
        if (body.customerId && body.customerId !== customerId) {
          tags.push({ type: 'SalesContactEmployment' as const, id: `CUSTOMER:${body.customerId}` });
        }
        return tags;
      },
    }),
    terminateSalesContactEmployment: builder.mutation<
      void,
      { id: number; contactId: number; customerId: number | null; body: SalesContactEmploymentTerminateRequest }
    >({
      query: ({ id, body }) => ({
        url: `/api/v1/sales-contacts/employments/${id}/terminate`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { contactId, customerId }) => {
        const tags = [
          { type: 'SalesContactEmployment' as const, id: `CONTACT:${contactId}` },
        ];
        if (customerId) tags.push({ type: 'SalesContactEmployment' as const, id: `CUSTOMER:${customerId}` });
        return tags;
      },
    }),
    deleteSalesContactEmployment: builder.mutation<
      void,
      { id: number; contactId: number; customerId: number | null }
    >({
      query: ({ id }) => ({ url: `/api/v1/sales-contacts/employments/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { contactId, customerId }) => {
        const tags = [
          { type: 'SalesContactEmployment' as const, id: `CONTACT:${contactId}` },
        ];
        if (customerId) tags.push({ type: 'SalesContactEmployment' as const, id: `CUSTOMER:${customerId}` });
        return tags;
      },
    }),
  }),
});

export const {
  useGetSalesContactsQuery,
  useGetSalesContactQuery,
  useCreateSalesContactMutation,
  useUpdateSalesContactMutation,
  useDeleteSalesContactMutation,
  useDeleteSalesContactsMutation,
  useUploadSalesContactsExcelMutation,
  useCheckSalesContactMobilePhoneAvailabilityQuery,
  useGetSalesContactEmploymentsByCustomerIdQuery,
  useCreateSalesContactEmploymentMutation,
  useUpdateSalesContactEmploymentMutation,
  useTerminateSalesContactEmploymentMutation,
  useDeleteSalesContactEmploymentMutation,
} = salesContactApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 * 필터 + 정렬 그대로 전송 (page/size 무시 — 필터링된 전체).
 */
export function useDownloadSalesContactsExcel() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (params: SalesContactListFilters & { sort?: string }) => {
      const response = await axiosInstance.get('/api/v1/sales-contacts/excel', {
        params: cleanParams(params),
        paramsSerializer: { indexes: null },
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });

      const filename =
        extractFilename(response.headers['content-disposition']) ??
        `sales-contacts_${todayStamp()}.xlsx`;

      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}

/**
 * 업로드 양식 (.xlsx) 다운로드 — 다운로드와 동일한 헤더 / 폭 / 톤. binary 응답이라 axios 직접 호출.
 */
export function useDownloadSalesContactsTemplate() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(async () => {
    const response = await axiosInstance.get('/api/v1/sales-contacts/excel/template', {
      responseType: 'blob',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const filename = extractFilename(response.headers['content-disposition'])
      ?? 'sales-contacts_template.xlsx';
    triggerBrowserDownload(response.data, filename);
  }, [token]);
}
