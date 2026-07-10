import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import BoardEditForm from '@/features/board/components/BoardEditForm/BoardEditForm';
import { useBoardEditPage } from '@/features/board/hooks/useBoardEditPage';

export default function BoardEditPage() {
  const { id } = useParams<{ id: string }>();
  const postId = Number(id);
  if (!postId) return null;

  const { queries } = useBoardEditPage(postId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <BoardEditForm id={postId} detail={detail} />}
    </QueryGate>
  );
}
