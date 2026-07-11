import { type DragEvent, type KeyboardEvent } from 'react';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import DeleteOutlineRoundedIcon from '@mui/icons-material/DeleteOutlineRounded';
import DragIndicatorRoundedIcon from '@mui/icons-material/DragIndicatorRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Muted from '@/shared/ui/atoms/Muted';
import {
  AddRow,
  CategoryRow,
  ContentBox,
  HelpText,
  OrderBadge,
  RowActions,
} from '@/pages/product/ProductCategoryPage.styles';
import type { ProductCategorySummary } from '@/features/product/types';
import type { useProductCategoryPage } from '@/features/product/hooks/useProductCategoryPage';

type PageState = ReturnType<typeof useProductCategoryPage>;

export interface ProductCategoryListProps {
  items: ProductCategorySummary[];
  canWrite: boolean;
  add: PageState['add'];
  edit: PageState['edit'];
  onDeleteRequest: (item: ProductCategorySummary) => void;
  draggedId: number | null;
  dragOverId: number | null;
  onDragStart: (e: DragEvent, id: number) => void;
  onDragOver: (e: DragEvent, id: number) => void;
  onDragLeave: () => void;
  onDrop: (id: number) => void;
}

/**
 * 제품 카테고리 목록 — DnD / 편집 state 는 outer page hook 이 owner. 컴포넌트는 렌더 + 이벤트 위임만.
 */
export default function ProductCategoryList({
  items,
  canWrite,
  add,
  edit,
  onDeleteRequest,
  draggedId,
  dragOverId,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
}: ProductCategoryListProps) {
  return (
    <ContentBox>
      <HelpText>
        {canWrite
          ? '드래그하여 노출 순서를 바꾸고, 이름 변경 / 삭제로 분류를 관리할 수 있습니다. 제품 모델이 참조 중인 카테고리는 삭제할 수 없습니다.'
          : '수정 권한이 없으므로 조회만 가능합니다.'}
      </HelpText>

      {canWrite && (
        <AddRow>
          <TextField
            size="small"
            fullWidth
            placeholder="새 카테고리명"
            value={add.value}
            onChange={(e) => add.onChange(e.target.value)}
            onKeyDown={(e: KeyboardEvent) => {
              if (e.key === 'Enter' && !e.nativeEvent.isComposing) add.onSubmit();
            }}
            slotProps={{ htmlInput: { maxLength: 50 } }}
          />
          <Button
            variant="contained"
            size="small"
            onClick={add.onSubmit}
            disabled={add.value.trim() === '' || add.isSubmitting}
            sx={{ flexShrink: 0, height: 40 }}
          >
            추가
          </Button>
        </AddRow>
      )}

      {items.length === 0 ? (
        <Typography sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>
          등록된 카테고리가 없습니다.
        </Typography>
      ) : (
        items.map((it, idx) => (
          <CategoryItem
            key={it.id}
            order={idx + 1}
            item={it}
            canWrite={canWrite}
            edit={edit}
            onDeleteRequest={onDeleteRequest}
            isDragging={draggedId === it.id}
            isDropTarget={dragOverId === it.id && draggedId !== it.id}
            onDragStart={onDragStart}
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}
          />
        ))
      )}
    </ContentBox>
  );
}

interface CategoryItemProps {
  order: number;
  item: ProductCategorySummary;
  canWrite: boolean;
  edit: PageState['edit'];
  onDeleteRequest: (item: ProductCategorySummary) => void;
  isDragging: boolean;
  isDropTarget: boolean;
  onDragStart: (e: DragEvent, id: number) => void;
  onDragOver: (e: DragEvent, id: number) => void;
  onDragLeave: () => void;
  onDrop: (id: number) => void;
}

function CategoryItem({
  order,
  item,
  canWrite,
  edit,
  onDeleteRequest,
  isDragging,
  isDropTarget,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
}: CategoryItemProps) {
  const isEditing = edit.editingId === item.id;

  return (
    <CategoryRow
      draggable={canWrite && !isEditing}
      isDragging={isDragging}
      isDropTarget={isDropTarget}
      canWrite={canWrite && !isEditing}
      onDragStart={(e) => onDragStart(e, item.id)}
      onDragOver={(e) => onDragOver(e, item.id)}
      onDragLeave={onDragLeave}
      onDrop={(e) => {
        e.preventDefault();
        onDrop(item.id);
      }}
    >
      {canWrite && (
        <DragIndicatorRoundedIcon sx={{ fontSize: 16, color: 'text.disabled', flexShrink: 0 }} />
      )}
      <OrderBadge>{order}</OrderBadge>

      {isEditing ? (
        <>
          <TextField
            size="small"
            fullWidth
            autoFocus
            value={edit.editingName}
            onChange={(e) => edit.onChangeName(e.target.value)}
            onKeyDown={(e: KeyboardEvent) => {
              if (e.key === 'Enter' && !e.nativeEvent.isComposing) edit.onSubmit();
              if (e.key === 'Escape') edit.onCancel();
            }}
            slotProps={{ htmlInput: { maxLength: 50 } }}
          />
          <RowActions>
            <IconButton size="small" color="primary" onClick={edit.onSubmit} aria-label="저장">
              <CheckRoundedIcon sx={{ fontSize: 18 }} />
            </IconButton>
            <IconButton size="small" onClick={edit.onCancel} aria-label="취소">
              <CloseRoundedIcon sx={{ fontSize: 18 }} />
            </IconButton>
          </RowActions>
        </>
      ) : (
        <>
          <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary', flexShrink: 0 }}>
            {item.name}
          </Typography>
          {item.productCount > 0 ? (
            <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary', flexShrink: 0 }}>
              모델 {item.productCount}개
            </Typography>
          ) : (
            <Muted>미사용</Muted>
          )}
          {canWrite && (
            <RowActions>
              <IconButton size="small" onClick={() => edit.onStart(item)} aria-label="이름 변경">
                <EditOutlinedIcon sx={{ fontSize: 18 }} />
              </IconButton>
              <IconButton
                size="small"
                onClick={() => onDeleteRequest(item)}
                disabled={item.productCount > 0}
                aria-label="삭제"
              >
                <DeleteOutlineRoundedIcon sx={{ fontSize: 18 }} />
              </IconButton>
            </RowActions>
          )}
        </>
      )}
    </CategoryRow>
  );
}
