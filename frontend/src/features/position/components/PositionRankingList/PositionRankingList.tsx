import { type DragEvent } from 'react';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import DragIndicatorRoundedIcon from '@mui/icons-material/DragIndicatorRounded';
import {
  ContentBox,
  HelpText,
  RankBadge,
  RankRow,
} from '@/pages/position/PositionRankingPage.styles';
import type { PositionSummary } from '@/features/position/types';

export interface PositionRankingListProps {
  items: PositionSummary[];
  canWrite: boolean;
  draggedId: number | null;
  dragOverId: number | null;
  onDragStart: (e: DragEvent, id: number) => void;
  onDragOver: (e: DragEvent, id: number) => void;
  onDragLeave: () => void;
  onDrop: (id: number) => void;
}

/**
 * 직책 서열 목록 — DnD state 는 outer page hook 이 owner. 컴포넌트는 렌더 + 이벤트 위임만.
 */
export default function PositionRankingList({
  items,
  canWrite,
  draggedId,
  dragOverId,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
}: PositionRankingListProps) {
  return (
    <ContentBox>
      <HelpText>
        {canWrite
          ? '드래그하여 직책 서열을 변경할 수 있습니다. 위로 올라갈수록 높은 직책입니다.'
          : '수정 권한이 없으므로 조회만 가능합니다.'}
      </HelpText>

      {items.length === 0 ? (
        <Typography sx={{ color: 'text.secondary', py: 4, textAlign: 'center' }}>
          등록된 직책이 없습니다.
        </Typography>
      ) : (
        items.map((it, idx) => (
          <RankItem
            key={it.id}
            rank={idx + 1}
            item={it}
            canWrite={canWrite}
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

interface RankItemProps {
  rank: number;
  item: PositionSummary;
  canWrite: boolean;
  isDragging: boolean;
  isDropTarget: boolean;
  onDragStart: (e: DragEvent, id: number) => void;
  onDragOver: (e: DragEvent, id: number) => void;
  onDragLeave: () => void;
  onDrop: (id: number) => void;
}

function RankItem({
  rank,
  item,
  canWrite,
  isDragging,
  isDropTarget,
  onDragStart,
  onDragOver,
  onDragLeave,
  onDrop,
}: RankItemProps) {
  const theme = useTheme();
  const tooltipPlacement = useMediaQuery(theme.breakpoints.up('md')) ? 'left' : 'top';

  const row = (
    <RankRow
      draggable={canWrite}
      isDragging={isDragging}
      isDropTarget={isDropTarget}
      canWrite={canWrite}
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
      <RankBadge>{rank}</RankBadge>
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary', flexShrink: 0 }}>
        {item.name}
      </Typography>
      <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary', flexShrink: 0 }}>
        ({item.code})
      </Typography>
      {item.description ? (
        <Typography
          sx={{
            fontSize: '0.75rem',
            color: 'text.secondary',
            marginLeft: 'auto',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {item.description}
        </Typography>
      ) : null}
    </RankRow>
  );

  if (!canWrite) return row;

  return (
    <Tooltip
      title={
        <Typography sx={{ fontSize: '0.75rem', fontWeight: 600 }}>드래그하여 서열 변경</Typography>
      }
      arrow
      placement={tooltipPlacement}
      enterDelay={500}
      enterNextDelay={300}
      disableInteractive
    >
      {row}
    </Tooltip>
  );
}
