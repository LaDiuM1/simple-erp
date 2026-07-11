import { api } from '@/shared/api/baseApi';
import { cleanParams } from '@/shared/api/cleanParams';
import type { PageResponse } from '@/shared/types/api';
import type {
  ExpenseCreateRequest,
  ExpenseDetail,
  ExpenseSearchParams,
  ExpenseSummary,
} from '../types';

const expenseApi = api.injectEndpoints({
  endpoints: (builder) => ({
    /**
     * 목록은 기본 본인 청구 (MINE) — 전체 (ALL) 는 EXPENSES 쓰기 권한자만 (BE 403 강제).
     * scope 필터 미선택 (null) 은 MINE 으로 매핑해 전송.
     */
    getExpenses: builder.query<PageResponse<ExpenseSummary>, ExpenseSearchParams>({
      query: ({ scope, ...params }) => ({
        url: '/api/v1/expenses',
        method: 'GET',
        params: cleanParams({ ...params, scope: scope ?? 'MINE' }),
      }),
      providesTags: (result) => [
        { type: 'Expense', id: 'LIST' },
        ...(result?.content.map((e) => ({ type: 'Expense' as const, id: e.id })) ?? []),
      ],
    }),
    getExpense: builder.query<ExpenseDetail, number>({
      query: (id) => ({ url: `/api/v1/expenses/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Expense', id }],
    }),
    /** 생성 = 즉시 상신 — 이후 상태 전이는 결재 결과 콜백으로만 (수정/삭제 엔드포인트 없음). */
    createExpense: builder.mutation<number, ExpenseCreateRequest>({
      query: (body) => ({ url: '/api/v1/expenses', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Expense', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetExpensesQuery,
  useGetExpenseQuery,
  useCreateExpenseMutation,
} = expenseApi;
