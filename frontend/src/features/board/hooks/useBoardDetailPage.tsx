import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useToggle } from '@/shared/hooks/useToggle';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import { useDeletePostMutation, useGetPostQuery } from '@/features/board/api/boardApi';
import { BOARD_CATEGORY_LABELS, type PostDetail } from '@/features/board/types';
import type { BoardDetailModalProps } from '@/features/board/components/BoardDetailModals/BoardDetailModals';
import { useCommentSection } from './useCommentSection';

/**
 * 게시글 상세 page hook — fetching / 본인 글 판단 / 삭제 confirm / 댓글 섹션 / headerActions 묶음.
 * 수정 / 삭제는 메뉴 write 권한이 아니라 작성자 본인 (authorId === 내 id) 기준 — BE 서비스도 동일 검증.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `postInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useBoardDetailPage(postId: number) {
  const navigate = useNavigate();
  const submit = useApiSubmit();
  const { data: profile } = useGetMyProfileQuery();

  const detailQuery = useGetPostQuery(postId, { skip: !postId });
  const commentSection = useCommentSection(postId, detailQuery.data?.comments ?? []);

  const [deleteConfirmOpen, deleteConfirm] = useToggle();
  const [deletePost, { isLoading: isDeleting }] = useDeletePostMutation();

  const isAuthor =
    profile != null && detailQuery.data != null && profile.id === detailQuery.data.authorId;

  // 삭제 완료까지 모달 유지 (confirmDisabled 로 재클릭 방지) — 실패 시 모달이 남아 재시도 가능.
  const handleConfirmedDelete = async () => {
    await submit(deletePost(postId), {
      success: '삭제되었습니다.',
      error: '삭제 중 오류가 발생했습니다.',
      navigateTo: MENU_PATH[MENU_CODE.BOARDS],
      onSuccess: deleteConfirm.off,
    });
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.BOARDS]),
    },
    ...(isAuthor
      ? [
          {
            design: 'create' as const,
            label: '수정',
            icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.BOARDS]}/${postId}/edit`),
          },
          {
            design: 'delete' as const,
            loading: isDeleting,
            onClick: deleteConfirm.on,
          },
        ]
      : []),
  ];

  const modal: BoardDetailModalProps = {
    deleteOpen: deleteConfirmOpen,
    isDeleting,
    deleteMessage: detailQuery.data
      ? `${detailQuery.data.title} 게시글을 삭제하시겠습니까?`
      : '게시글을 삭제하시겠습니까?',
    onConfirmDelete: handleConfirmedDelete,
    onCloseDelete: deleteConfirm.off,
  };

  return {
    queries: { detail: detailQuery },
    headerActions,
    commentSection: commentSection.section,
    modal,
  };
}

export function postInfoFields(d: PostDetail): HeaderDetailField[] {
  return [
    { label: '카테고리', value: BOARD_CATEGORY_LABELS[d.category] },
    { label: '제목', value: d.title },
    { label: '작성자', value: d.authorName },
    { label: '작성일', value: formatDateTime(d.createdAt) },
  ];
}
