import { useBoardEditForm } from '@/features/board/hooks/useBoardEditForm';
import type { PostDetail } from '@/features/board/types';
import BoardForm from '../BoardForm/BoardForm';

/**
 * 게시글 수정 폼 Body — outer (page) 가 detail 보장한 뒤 위임. form-state hook 의 invariant 충족.
 */
export default function BoardEditForm({ id, detail }: { id: number; detail: PostDetail }) {
  const form = useBoardEditForm(id, detail);
  return <BoardForm form={form} mode="edit" />;
}
