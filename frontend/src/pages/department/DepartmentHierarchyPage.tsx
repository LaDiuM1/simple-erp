import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import HierarchyTree from '@/features/department/components/HierarchyTree/HierarchyTree';
import { useDepartmentHierarchyPage } from '@/features/department/hooks/useDepartmentHierarchyPage';
import { PageRoot, PageSurface } from './DepartmentHierarchyPage.styles';

/**
 * 부서 계층 관리 페이지 — DnD 트리. 본문은 HierarchyTree 컴포넌트로, DnD/state/mutation 은 page hook 으로.
 */
export default function DepartmentHierarchyPage() {
  const {
    queries,
    rootDepts,
    childrenByParent,
    canWrite,
    draggedId,
    dragOverKey,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDragLeave,
    onDrop,
    headerActions,
  } = useDepartmentHierarchyPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <PageRoot>
        <PageSurface onDragEnd={onDragEnd}>
          <QueryGate queries={queries}>
            {() => (
              <HierarchyTree
                rootDepts={rootDepts}
                childrenByParent={childrenByParent}
                canWrite={canWrite}
                draggedId={draggedId}
                dragOverKey={dragOverKey}
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
