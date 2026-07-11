import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import { postInfoFields, useBoardDetailPage } from '@/features/board/hooks/useBoardDetailPage';
import PostBody from '@/features/board/components/PostBody/PostBody';
import CommentSection from '@/features/board/components/CommentSection/CommentSection';
import BoardDetailModals from '@/features/board/components/BoardDetailModals/BoardDetailModals';
import { DetailRoot } from '@/features/board/components/boardDetail.styles';

export default function BoardDetailPage() {
  const { id } = useParams<{ id: string }>();
  const postId = Number(id);
  if (!postId) return null;

  const { queries, headerActions, commentSection, modal } = useBoardDetailPage(postId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={postInfoFields(detail)} />
            <PostBody detail={detail} />
            <CommentSection section={commentSection} />
          </DetailRoot>
        )}
      </QueryGate>
      <BoardDetailModals modal={modal} />
    </>
  );
}
