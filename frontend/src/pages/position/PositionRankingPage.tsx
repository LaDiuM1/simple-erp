import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import PositionRankingList from '@/features/position/components/PositionRankingList/PositionRankingList';
import { usePositionRankingPage } from '@/features/position/hooks/usePositionRankingPage';
import { PageRoot, PageSurface } from './PositionRankingPage.styles';

/**
 * 직책 서열 관리 페이지 — DnD 트리. 본문은 PositionRankingList 컴포넌트로, DnD/state/mutation 은 page hook 으로.
 */
export default function PositionRankingPage() {
  const {
    queries,
    items,
    canWrite,
    draggedId,
    dragOverId,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDragLeave,
    onDrop,
    headerActions,
  } = usePositionRankingPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <PageRoot>
        <PageSurface onDragEnd={onDragEnd}>
          <QueryGate queries={queries}>
            {() => (
              <PositionRankingList
                items={items}
                canWrite={canWrite}
                draggedId={draggedId}
                dragOverId={dragOverId}
                onDragStart={onDragStart}
                onDragOver={onDragOver}
                onDragLeave={onDragLeave}
                onDrop={onDrop}
              />
            )}
          </QueryGate>
        </PageSurface>
      </PageRoot>
    </>
  );
}
