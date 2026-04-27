import { api } from '@/shared/api/baseApi';
import type {
  SalesActivity,
  SalesActivityCreateRequest,
  SalesActivityUpdateRequest,
  SalesAssignmentCreateRequest,
  SalesAssignmentTerminateRequest,
  SalesAssignmentUpdateRequest,
  SalesCustomerAggregate,
  SalesCustomerDetail,
} from '../types';

const salesCustomerApi = api.injectEndpoints({
  endpoints: (builder) => ({
    /**
     * 고객사 ID 배열에 대한 영업 집계 (활성 담당자 / 활동 카운트 / 마지막 활동일).
     * 목록 페이지가 customer 마스터 페이지와 합성하기 위한 보강 데이터.
     */
    getSalesCustomerAggregates: builder.query<SalesCustomerAggregate[], number[]>({
      query: (customerIds) => ({
        url: '/api/v1/sales-customers/aggregates',
        method: 'GET',
        params: { customerIds: customerIds.join(',') },
      }),
      providesTags: (_result, _error, customerIds) =>
        customerIds.flatMap((id) => [
          { type: 'SalesAggregate' as const, id },
          { type: 'SalesActivity' as const, id: `CUSTOMER:${id}` },
          { type: 'SalesAssignment' as const, id: `CUSTOMER:${id}` },
        ]),
    }),

    getSalesCustomerDetail: builder.query<SalesCustomerDetail, number>({
      query: (customerId) => ({
        url: `/api/v1/sales-customers/${customerId}`,
        method: 'GET',
      }),
      providesTags: (_result, _error, customerId) => [
        { type: 'SalesActivity', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAssignment', id: `CUSTOMER:${customerId}` },
      ],
    }),

    /**
     * 영업 명부 상세 페이지가 호출 — 해당 명부 인물이 등장한 활동 이력 (모든 고객사 통합).
     */
    getSalesActivitiesByContact: builder.query<SalesActivity[], number>({
      query: (contactId) => ({
        url: `/api/v1/sales-customers/contacts/${contactId}/activities`,
        method: 'GET',
      }),
      providesTags: (_result, _error, contactId) => [
        { type: 'SalesActivity', id: `CONTACT:${contactId}` },
      ],
    }),

    createSalesActivity: builder.mutation<number, SalesActivityCreateRequest>({
      query: (body) => ({ url: '/api/v1/sales-customers/activities', method: 'POST', data: body }),
      invalidatesTags: (_result, _error, { customerId, customerContactId }) => [
        { type: 'SalesActivity', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
        ...(customerContactId
          ? [{ type: 'SalesActivity' as const, id: `CONTACT:${customerContactId}` }]
          : []),
      ],
    }),
    updateSalesActivity: builder.mutation<
      void,
      { id: number; customerId: number; body: SalesActivityUpdateRequest }
    >({
      query: ({ id, body }) => ({
        url: `/api/v1/sales-customers/activities/${id}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { customerId, body }) => [
        { type: 'SalesActivity', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
        ...(body.customerContactId
          ? [{ type: 'SalesActivity' as const, id: `CONTACT:${body.customerContactId}` }]
          : []),
      ],
    }),
    deleteSalesActivity: builder.mutation<
      void,
      { id: number; customerId: number; customerContactId?: number | null }
    >({
      query: ({ id }) => ({ url: `/api/v1/sales-customers/activities/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { customerId, customerContactId }) => [
        { type: 'SalesActivity', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
        ...(customerContactId
          ? [{ type: 'SalesActivity' as const, id: `CONTACT:${customerContactId}` }]
          : []),
      ],
    }),

    createSalesAssignment: builder.mutation<number, SalesAssignmentCreateRequest>({
      query: (body) => ({ url: '/api/v1/sales-customers/assignments', method: 'POST', data: body }),
      invalidatesTags: (_result, _error, { customerId }) => [
        { type: 'SalesAssignment', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
      ],
    }),
    updateSalesAssignment: builder.mutation<
      void,
      { id: number; customerId: number; body: SalesAssignmentUpdateRequest }
    >({
      query: ({ id, body }) => ({
        url: `/api/v1/sales-customers/assignments/${id}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { customerId }) => [
        { type: 'SalesAssignment', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
      ],
    }),
    terminateSalesAssignment: builder.mutation<
      void,
      { id: number; customerId: number; body: SalesAssignmentTerminateRequest }
    >({
      query: ({ id, body }) => ({
        url: `/api/v1/sales-customers/assignments/${id}/terminate`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: (_result, _error, { customerId }) => [
        { type: 'SalesAssignment', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
      ],
    }),
    deleteSalesAssignment: builder.mutation<void, { id: number; customerId: number }>({
      query: ({ id }) => ({ url: `/api/v1/sales-customers/assignments/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, { customerId }) => [
        { type: 'SalesAssignment', id: `CUSTOMER:${customerId}` },
        { type: 'SalesAggregate', id: customerId },
      ],
    }),
  }),
});

export const {
  useGetSalesCustomerAggregatesQuery,
  useGetSalesCustomerDetailQuery,
  useGetSalesActivitiesByContactQuery,
  useCreateSalesActivityMutation,
  useUpdateSalesActivityMutation,
  useDeleteSalesActivityMutation,
  useCreateSalesAssignmentMutation,
  useUpdateSalesAssignmentMutation,
  useTerminateSalesAssignmentMutation,
  useDeleteSalesAssignmentMutation,
} = salesCustomerApi;
