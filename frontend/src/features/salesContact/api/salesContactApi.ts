import { api } from '@/shared/api/baseApi';
import type { PageResponse } from '@/shared/types/api';
import type {
  SalesContactCreateRequest,
  SalesContactDetail,
  SalesContactEmployment,
  SalesContactEmploymentCreateRequest,
  SalesContactEmploymentTerminateRequest,
  SalesContactEmploymentUpdateRequest,
  SalesContactSearchParams,
  SalesContactSummary,
  SalesContactUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const salesContactApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getSalesContacts: builder.query<PageResponse<SalesContactSummary>, SalesContactSearchParams>({
      query: (params) => ({
        url: '/api/v1/sales-contacts',
        method: 'GET',
        params: cleanParams(params),
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
  useGetSalesContactEmploymentsByCustomerIdQuery,
  useCreateSalesContactEmploymentMutation,
  useUpdateSalesContactEmploymentMutation,
  useTerminateSalesContactEmploymentMutation,
  useDeleteSalesContactEmploymentMutation,
} = salesContactApi;
