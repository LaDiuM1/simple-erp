import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ProductCategoryList from '@/features/product/components/ProductCategoryList/ProductCategoryList';
import { useProductCategoryPage } from '@/features/product/hooks/useProductCategoryPage';
import { PageRoot, PageSurface } from './ProductCategoryPage.styles';

/**
 * 제품 카테고리 관리 페이지 — 제품 모델 관리 (PRODUCTS) 의 서브 기능.
 * DnD / 인라인 편집 / 삭제 state 는 page hook 으로, 본문은 ProductCategoryList 컴포넌트로.
 */
export default function ProductCategoryPage() {
  const {
    queries,
    items,
    canWrite,
    add,
    edit,
    remove,
    draggedId,
    dragOverId,
    onDragStart,
    onDragEnd,
    onDragOver,
    onDragLeave,
    onDrop,
    headerActions,
  } = useProductCategoryPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <PageRoot>
        <PageSurface onDragEnd={onDragEnd}>
          <QueryGate queries={queries}>
            {() => (
              <ProductCategoryList
                items={items}
                canWrite={canWrite}
                add={add}
                edit={edit}
                onDeleteRequest={remove.onRequest}
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
      <ConfirmModal
        isOpen={remove.target !== null}
        title="카테고리 삭제"
        message={remove.target ? `'${remove.target.name}' 카테고리를 삭제하시겠습니까?` : ''}
        confirmLabel="삭제"
        danger
        confirmDisabled={remove.isDeleting}
        onConfirm={remove.onConfirm}
        onCancel={remove.onCancel}
      />
    </>
  );
}
