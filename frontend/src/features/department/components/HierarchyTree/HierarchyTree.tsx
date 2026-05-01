import { type DragEvent } from 'react';
import Box from '@mui/material/Box';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import DragIndicatorRoundedIcon from '@mui/icons-material/DragIndicatorRounded';
import FolderRoundedIcon from '@mui/icons-material/FolderRounded';
import HomeRoundedIcon from '@mui/icons-material/HomeRounded';
import { ContentBox, NodeRow } from '@/pages/department/DepartmentHierarchyPage.styles';
import type { DepartmentInfo } from '@/features/reference/types';

export const ROOT_KEY = 'root' as const;
export type DragOverKey = number | typeof ROOT_KEY | null;

export interface HierarchyTreeProps {
  rootDepts: DepartmentInfo[];
  childrenByParent: Map<number | null, DepartmentInfo[]>;
  canWrite: boolean;
  draggedId: number | null;
  dragOverKey: DragOverKey;
  onDragStart: (e: DragEvent, id: number) => void;
  onDragOver: (e: DragEvent, key: number | typeof ROOT_KEY) => void;
  onDragLeave: () => void;
  onDrop: (targetParentId: number | null) => void;
}

/**
 * 부서 계층 트리 — DnD state/mutation 은 outer page hook 이 owner.
 * 컴포넌트는 렌더 + 이벤트 위임만.
 */
export default function HierarchyTree({
  rootDepts,
  childrenByParent,
  canWrite,
  draggedId,
  dragOverKey,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
}: HierarchyTreeProps) {
  return (
    <ContentBox>
      {canWrite && (
        <RootDropZone
          isOver={dragOverKey === ROOT_KEY}
          onDragOver={(e) => onDragOver(e, ROOT_KEY)}
          onDragLeave={onDragLeave}
          onDrop={() => onDrop(null)}
        />
      )}

      {rootDepts.length === 0 ? (
        <Typography sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>
          등록된 부서가 없습니다.
        </Typography>
      ) : (
        rootDepts.map((dept) => (
          <TreeNode
            key={dept.id}
            dept={dept}
            level={0}
            childrenByParent={childrenByParent}
            canWrite={canWrite}
            draggedId={draggedId}
            dragOverKey={dragOverKey}
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

interface TreeNodeProps {
  dept: DepartmentInfo;
  level: number;
  childrenByParent: Map<number | null, DepartmentInfo[]>;
  canWrite: boolean;
  draggedId: number | null;
  dragOverKey: DragOverKey;
  onDragStart: (e: DragEvent, id: number) => void;
  onDragOver: (e: DragEvent, key: number | typeof ROOT_KEY) => void;
  onDragLeave: () => void;
  onDrop: (targetParentId: number | null) => void;
}

function TreeNode({
  dept,
  level,
  childrenByParent,
  canWrite,
  draggedId,
  dragOverKey,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
}: TreeNodeProps) {
  const theme = useTheme();
  const tooltipPlacement = useMediaQuery(theme.breakpoints.up('md')) ? 'left' : 'top';

  const children = childrenByParent.get(dept.id) ?? [];
  const isDragging = draggedId === dept.id;
  const isDropTarget = dragOverKey === dept.id;

  const row = (
    <NodeRow
      draggable={canWrite}
      level={level}
      isDragging={isDragging}
      isDropTarget={isDropTarget}
      canWrite={canWrite}
      onDragStart={(e) => onDragStart(e, dept.id)}
      onDragOver={(e) => onDragOver(e, dept.id)}
      onDragLeave={onDragLeave}
      onDrop={(e) => {
        e.preventDefault();
        onDrop(dept.id);
      }}
    >
      {canWrite && (
        <DragIndicatorRoundedIcon sx={{ fontSize: 16, color: 'text.disabled', flexShrink: 0 }} />
      )}
      <FolderRoundedIcon
        sx={{
          fontSize: 18,
          color: level === 0 ? 'primary.main' : 'text.disabled',
          flexShrink: 0,
        }}
      />
      <Typography
        sx={{
          fontSize: '0.875rem',
          fontWeight: level === 0 ? 600 : 400,
          color: 'text.primary',
        }}
      >
        {dept.name}
      </Typography>
      <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary' }}>({dept.code})</Typography>
    </NodeRow>
  );

  return (
    <Box>
      {canWrite ? (
        <Tooltip
          title={
            <Box sx={{ lineHeight: 1.5 }}>
              <Typography sx={{ fontSize: '0.75rem', fontWeight: 600, color: 'inherit' }}>
                드래그하여 계층 관계를 변경
              </Typography>
              <Typography
                sx={{ fontSize: '0.6875rem', color: 'inherit', opacity: 0.8, mt: '0.125rem' }}
              >
                동일 계층 내 순서는 변경되지 않습니다.
              </Typography>
            </Box>
          }
          arrow
          placement={tooltipPlacement}
          enterDelay={500}
          enterNextDelay={300}
          disableInteractive
        >
          {row}
        </Tooltip>
      ) : (
        row
      )}
      {children.map((child) => (
        <TreeNode
          key={child.id}
          dept={child}
          level={level + 1}
          childrenByParent={childrenByParent}
          canWrite={canWrite}
          draggedId={draggedId}
          dragOverKey={dragOverKey}
          onDragStart={onDragStart}
          onDragOver={onDragOver}
          onDragLeave={onDragLeave}
          onDrop={onDrop}
        />
      ))}
    </Box>
  );
}

interface RootDropZoneProps {
  isOver: boolean;
  onDragOver: (e: DragEvent) => void;
  onDragLeave: () => void;
  onDrop: () => void;
}

function RootDropZone({ isOver, onDragOver, onDragLeave, onDrop }: RootDropZoneProps) {
  return (
    <Box
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={(e) => {
        e.preventDefault();
        onDrop();
      }}
      sx={(theme) => ({
        display: 'flex',
        alignItems: 'center',
        gap: 1,
        padding: '0.625rem 0.875rem',
        marginBottom: '0.5rem',
        borderRadius: '4px',
        border: '1px dashed',
        borderColor: isOver ? theme.palette.primary.main : theme.palette.divider,
        backgroundColor: isOver ? theme.palette.primarySubtle : 'transparent',
        color: isOver ? theme.palette.primary.main : theme.palette.text.secondary,
        transition: 'background-color 0.15s, border-color 0.15s, color 0.15s',
      })}
    >
      <HomeRoundedIcon sx={{ fontSize: 18 }} />
      <Typography sx={{ fontSize: '0.8125rem', userSelect: 'none' }}>
        최상위 부서로 이동
      </Typography>
    </Box>
  );
}

export function buildChildrenMap(
  depts: DepartmentInfo[],
): Map<number | null, DepartmentInfo[]> {
  const map = new Map<number | null, DepartmentInfo[]>();
  for (const dept of depts) {
    const key = dept.parentId ?? null;
    const list = map.get(key) ?? [];
    list.push(dept);
    map.set(key, list);
  }
  for (const list of map.values()) {
    list.sort((a, b) => a.code.localeCompare(b.code));
  }
  return map;
}

export function isDescendant(
  ancestorId: number,
  candidateId: number,
  childrenByParent: Map<number | null, DepartmentInfo[]>,
): boolean {
  const stack: number[] = [ancestorId];
  while (stack.length > 0) {
    const id = stack.pop() as number;
    const children = childrenByParent.get(id) ?? [];
    for (const child of children) {
      if (child.id === candidateId) return true;
      stack.push(child.id);
    }
  }
  return false;
}
