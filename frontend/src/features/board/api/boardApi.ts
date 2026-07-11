import { api } from '@/shared/api/baseApi';
import { cleanParams } from '@/shared/api/cleanParams';
import type { PageResponse } from '@/shared/types/api';
import type {
  CommentCreateRequest,
  PostCreateRequest,
  PostDetail,
  PostSearchParams,
  PostSummary,
  PostUpdateRequest,
} from '../types';

const boardApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getPosts: builder.query<PageResponse<PostSummary>, PostSearchParams>({
      query: (params) => ({
        url: '/api/v1/boards',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Post', id: 'LIST' },
        ...(result?.content.map((p) => ({ type: 'Post' as const, id: p.id })) ?? []),
      ],
    }),
    getPost: builder.query<PostDetail, number>({
      query: (id) => ({ url: `/api/v1/boards/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Post', id }],
    }),
    createPost: builder.mutation<number, PostCreateRequest>({
      query: (body) => ({ url: '/api/v1/boards', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Post', id: 'LIST' }],
    }),
    updatePost: builder.mutation<void, { id: number; body: PostUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/boards/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Post', id },
        { type: 'Post', id: 'LIST' },
      ],
    }),
    deletePost: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/boards/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Post', id: 'LIST' }],
    }),

    /** 댓글 변화는 상세 (댓글 목록) 와 목록 (commentCount) 양쪽 invalidate. */
    createPostComment: builder.mutation<number, { postId: number; body: CommentCreateRequest }>({
      query: ({ postId, body }) => ({
        url: `/api/v1/boards/${postId}/comments`,
        method: 'POST',
        data: body,
      }),
      invalidatesTags: (_result, _error, { postId }) => [
        { type: 'Post', id: postId },
        { type: 'Post', id: 'LIST' },
      ],
    }),
    deletePostComment: builder.mutation<void, { postId: number; commentId: number }>({
      query: ({ postId, commentId }) => ({
        url: `/api/v1/boards/${postId}/comments/${commentId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, { postId }) => [
        { type: 'Post', id: postId },
        { type: 'Post', id: 'LIST' },
      ],
    }),
  }),
});

export const {
  useGetPostsQuery,
  useGetPostQuery,
  useCreatePostMutation,
  useUpdatePostMutation,
  useDeletePostMutation,
  useCreatePostCommentMutation,
  useDeletePostCommentMutation,
} = boardApi;
