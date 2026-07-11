import TextField from '@mui/material/TextField';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import type { CommentSectionProps } from '@/features/board/hooks/useCommentSection';
import {
  CommentAuthor,
  CommentContent,
  CommentCount,
  CommentDate,
  CommentDeleteButton,
  CommentForm,
  CommentMeta,
  CommentRow,
  CommentSubmitButton,
  EmptyComments,
  SectionHeader,
  SectionRoot,
  SectionTitle,
} from './CommentSection.styles';

/**
 * 댓글 섹션 — 목록 / 본인 댓글 삭제 (ConfirmModal) / 하단 입력 + 등록.
 * state 와 handler 는 useCommentSection hook 이 소유, 여기서는 렌더만.
 */
export default function CommentSection({ section }: { section: CommentSectionProps }) {
  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>댓글</SectionTitle>
        {section.comments.length > 0 && <CommentCount>{section.comments.length}</CommentCount>}
      </SectionHeader>

      {section.comments.length === 0 ? (
        <EmptyComments>등록된 댓글이 없습니다.</EmptyComments>
      ) : (
        section.comments.map((comment) => (
          <CommentRow key={comment.id}>
            <CommentMeta>
              <CommentAuthor>{comment.authorName}</CommentAuthor>
              <CommentDate>{formatDateTime(comment.createdAt)}</CommentDate>
              {section.myId === comment.authorId && (
                <CommentDeleteButton
                  size="small"
                  aria-label="댓글 삭제"
                  onClick={() => section.onRequestDelete(comment)}
                >
                  <DeleteOutlineIcon sx={{ fontSize: '1rem' }} />
                </CommentDeleteButton>
              )}
            </CommentMeta>
            <CommentContent>{comment.content}</CommentContent>
          </CommentRow>
        ))
      )}

      <CommentForm onSubmit={section.onSubmit}>
        <TextField
          size="small"
          fullWidth
          multiline
          maxRows={4}
          placeholder="댓글을 입력해주세요."
          value={section.input}
          onChange={(e) => section.onInputChange(e.target.value)}
          slotProps={{ htmlInput: { maxLength: 1000 } }}
        />
        <CommentSubmitButton
          type="submit"
          disabled={section.isSubmitting || section.input.trim() === ''}
        >
          {section.isSubmitting ? '등록 중...' : '등록'}
        </CommentSubmitButton>
      </CommentForm>

      <ConfirmModal
        isOpen={section.deleteTarget !== null}
        title="댓글 삭제"
        message="댓글을 삭제하시겠습니까?"
        confirmLabel={section.isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={section.onConfirmDelete}
        onCancel={section.onCancelDelete}
      />
    </SectionRoot>
  );
}
