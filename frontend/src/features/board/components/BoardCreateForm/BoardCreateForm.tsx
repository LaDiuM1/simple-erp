import { useBoardCreateForm } from '@/features/board/hooks/useBoardCreateForm';
import BoardForm from '../BoardForm/BoardForm';

export default function BoardCreateForm() {
  const form = useBoardCreateForm();
  return <BoardForm form={form} mode="create" />;
}
