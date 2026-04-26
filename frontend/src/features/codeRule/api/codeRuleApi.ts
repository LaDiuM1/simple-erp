import { api } from '@/shared/api/baseApi';
import type {
  CodeRule,
  CodeRulePreviewRequest,
  CodeRulePreviewResponse,
  CodeRuleTarget,
  CodeRuleUpdateRequest,
} from '../types';

const codeRuleApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getCodeRules: builder.query<CodeRule[], void>({
      query: () => ({ url: '/api/v1/code-rules', method: 'GET' }),
      providesTags: (result) => [
        { type: 'CodeRule', id: 'LIST' },
        ...(result?.map((r) => ({ type: 'CodeRule' as const, id: r.target })) ?? []),
      ],
    }),
    getCodeRule: builder.query<CodeRule, CodeRuleTarget>({
      query: (target) => ({ url: `/api/v1/code-rules/${target}`, method: 'GET' }),
      providesTags: (_result, _error, target) => [{ type: 'CodeRule', id: target }],
    }),
    updateCodeRule: builder.mutation<CodeRule, { target: CodeRuleTarget; body: CodeRuleUpdateRequest }>({
      query: ({ target, body }) => ({ url: `/api/v1/code-rules/${target}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { target }) => [
        { type: 'CodeRule', id: target },
        { type: 'CodeRule', id: 'LIST' },
      ],
    }),
    previewCodeRule: builder.mutation<
      CodeRulePreviewResponse,
      { target: CodeRuleTarget; body: CodeRulePreviewRequest }
    >({
      query: ({ target, body }) => ({ url: `/api/v1/code-rules/${target}/preview`, method: 'POST', data: body }),
    }),
  }),
});

export const {
  useGetCodeRulesQuery,
  useGetCodeRuleQuery,
  useUpdateCodeRuleMutation,
  usePreviewCodeRuleMutation,
} = codeRuleApi;
