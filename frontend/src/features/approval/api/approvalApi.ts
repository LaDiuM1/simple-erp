import { api } from '@/shared/api/baseApi';
import { cleanParams } from '@/shared/api/cleanParams';
import type { PageResponse } from '@/shared/types/api';
import type {
  ApprovalCreateRequest,
  ApprovalDecisionRequest,
  ApprovalDetail,
  ApprovalSearchParams,
  ApprovalSummary,
} from '../types';

/**
 * 결재 결정 (승인/반려/취소) 은 BE 콜백으로 경비/휴가 상태가 같은 트랜잭션에 바뀐다 —
 * 연관 도메인 캐시도 함께 무효화. Expense 는 대상 claim id (refId) 를 mutation 시점에 모르므로
 * 타입 전체 무효화, Leave / LeaveBalance 는 attendanceApi 의 'MY' id 체계를 따른다.
 */
const decisionSideEffectTags = [
  'Expense' as const,
  { type: 'Leave' as const, id: 'MY' },
  { type: 'LeaveBalance' as const, id: 'MY' },
];

const approvalApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getApprovals: builder.query<PageResponse<ApprovalSummary>, ApprovalSearchParams>({
      // box 는 BE 필수 파라미터 — 필터 미선택 (null = '전체') 은 INVOLVED 로 매핑해 전송
      query: ({ box, ...params }) => ({
        url: '/api/v1/approvals',
        method: 'GET',
        params: cleanParams({ ...params, box: box ?? 'INVOLVED' }),
      }),
      providesTags: (result) => [
        { type: 'Approval', id: 'LIST' },
        ...(result?.content.map((a) => ({ type: 'Approval' as const, id: a.id })) ?? []),
      ],
    }),
    getApproval: builder.query<ApprovalDetail, number>({
      query: (id) => ({ url: `/api/v1/approvals/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Approval', id }],
    }),
    createApproval: builder.mutation<number, ApprovalCreateRequest>({
      query: (body) => ({ url: '/api/v1/approvals', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Approval', id: 'LIST' }],
    }),
    approveApproval: builder.mutation<void, { id: number; body: ApprovalDecisionRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/approvals/${id}/approve`, method: 'POST', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Approval', id },
        { type: 'Approval', id: 'LIST' },
        ...decisionSideEffectTags,
      ],
    }),
    rejectApproval: builder.mutation<void, { id: number; body: ApprovalDecisionRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/approvals/${id}/reject`, method: 'POST', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Approval', id },
        { type: 'Approval', id: 'LIST' },
        ...decisionSideEffectTags,
      ],
    }),
    cancelApproval: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/approvals/${id}/cancel`, method: 'POST' }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'Approval', id },
        { type: 'Approval', id: 'LIST' },
        ...decisionSideEffectTags,
      ],
    }),
  }),
});

export const {
  useGetApprovalsQuery,
  useGetApprovalQuery,
  useCreateApprovalMutation,
  useApproveApprovalMutation,
  useRejectApprovalMutation,
  useCancelApprovalMutation,
} = approvalApi;
