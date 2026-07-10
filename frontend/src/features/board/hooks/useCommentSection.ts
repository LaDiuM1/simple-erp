import * as React from 'react';
import { useState } from 'react';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import {
  useCreatePostCommentMutation,
  useDeletePostCommentMutation,
} from '@/features/board/api/boardApi';
import type { PostComment } from '@/features/board/types';

/** CommentSection 컴포넌트가 받는 prop 객체 — state + handler + mutation 묶음. */
export interface CommentSectionProps {
  comments: PostComment[];
  /** 프로필 로드 전에는 null — 본인 댓글 삭제 버튼 미노출. */
  myId: number | null;
  input: string;
  onInputChange: (value: string) => void;
  isSubmitting: boolean;
  onSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  deleteTarget: PostComment | null;
  isDeleting: boolean;
  onRequestDelete: (comment: PostComment) => void;
  onCancelDelete: () => void;
  onConfirmDelete: () => void;
}

/**
 * 댓글 섹션 hook — 입력 / 등록 / 본인 댓글 삭제 confirm 묶음.
 * 댓글 목록은 상세 응답에 포함되므로 detail 을 가진 page hook 이 넘겨준다.
 */
export function useCommentSection(
  postId: number,
  comments: PostComment[],
): { section: CommentSectionProps } {
  const submit = useApiSubmit();
  const { data: profile } = useGetMyProfileQuery();

  const [input, setInput] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<PostComment | null>(null);
  const [createComment, { isLoading: isSubmitting }] = useCreatePostCommentMutation();
  const [deleteComment, { isLoading: isDeleting }] = useDeletePostCommentMutation();

  const onSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    const content = input.trim();
    if (content === '' || isSubmitting) return;
    void submit(createComment({ postId, body: { content } }), {
      success: '댓글이 등록되었습니다.',
      error: '댓글 등록 중 오류가 발생했습니다.',
      onSuccess: () => setInput(''),
    });
  };

  const onConfirmDelete = () => {
    if (!deleteTarget || isDeleting) return;
    void submit(deleteComment({ postId, commentId: deleteTarget.id }), {
      success: '댓글이 삭제되었습니다.',
      error: '댓글 삭제 중 오류가 발생했습니다.',
      onSuccess: () => setDeleteTarget(null),
    });
  };

  return {
    section: {
      comments,
      myId: profile?.id ?? null,
      input,
      onInputChange: setInput,
      isSubmitting,
      onSubmit,
      deleteTarget,
      isDeleting,
      onRequestDelete: setDeleteTarget,
      onCancelDelete: () => setDeleteTarget(null),
      onConfirmDelete,
    },
  };
}
