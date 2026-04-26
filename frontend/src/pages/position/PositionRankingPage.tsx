import { type DragEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import DragIndicatorRoundedIcon from '@mui/icons-material/DragIndicatorRounded';
import { ContentBox, HelpText, PageRoot, PageSurface, RankBadge, RankRow, } from './PositionRankingPage.styles';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useGetPositionsRankingQuery, useReorderPositionsMutation, } from '@/features/position/api/positionApi';
import type { PositionSummary } from '@/features/position/types';
import { getErrorMessage } from '@/shared/api/error';

/**
 * 직책 서열 관리 페이지.
 * - 서버에서 받은 직책을 rankLevel 오름차순으로 세로 정렬
 * - canWrite 일 때만 HTML5 native DnD 활성화 — 다른 행 위 드롭 = 그 자리에 끼움
 * - 드롭 시 즉시 PUT /positions/ranking 호출. 실패하면 로컬 순서 롤백
 * - 서열은 페이지가 1, 2, 3 ... 순으로 화면에서 재계산해 보여주고, 서버는 동일한 결과를 응답으로 반영
 */
export default function PositionRankingPage() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const { canWrite } = usePermission(MENU_CODE.POSITIONS);
  const { data: serverList = [], isLoading, isError, refetch, error } = useGetPositionsRankingQuery();
  const [reorderPositions] = useReorderPositionsMutation();

  // 드래그 중 즉각 반영을 위한 로컬 사본. 서버 데이터가 갱신되면 동기화.
  const [items, setItems] = useState<PositionSummary[]>([]);
  useEffect(() => {
    setItems(serverList);
  }, [serverList]);

  const [draggedId, setDraggedId] = useState<number | null>(null);
  const [dragOverId, setDragOverId] = useState<number | null>(null);

  const handleDragStart = (e: DragEvent, id: number) => {
    if (!canWrite) return;
    e.dataTransfer.effectAllowed = 'move';
    setDraggedId(id);
  };

  const handleDragEnd = () => {
    setDraggedId(null);
    setDragOverId(null);
  };

  const handleDragOver = (e: DragEvent, id: number) => {
    if (!canWrite || draggedId == null) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    if (id !== dragOverId) setDragOverId(id);
  };

  const handleDragLeave = () => {
    setDragOverId(null);
  };

  const handleDrop = async (targetId: number) => {
    setDragOverId(null);
    const sourceId = draggedId;
    setDraggedId(null);

    if (sourceId == null || sourceId === targetId) return;

    const fromIndex = items.findIndex((it) => it.id === sourceId);
    const toIndex = items.findIndex((it) => it.id === targetId);
    if (fromIndex === -1 || toIndex === -1 || fromIndex === toIndex) return;

    const previous = items;
    const next = [...items];
    const [moved] = next.splice(fromIndex, 1);
    next.splice(toIndex, 0, moved);
    setItems(next);

    try {
      await reorderPositions({ orderedIds: next.map((it) => it.id) }).unwrap();
      snackbar.success('직책 서열이 변경되었습니다.');
    } catch (err) {
      setItems(previous);
      snackbar.error(getErrorMessage(err, '서열 변경 중 오류가 발생했습니다.'));
    }
  };

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel' as const,
            label: '목록으로',
            onClick: () => navigate(MENU_PATH[MENU_CODE.POSITIONS]),
          },
        ]}
      />

      <PageRoot>
        <PageSurface onDragEnd={handleDragEnd}>
          {isLoading ? (
            <LoadingScreen />
          ) : isError ? (
            <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />
          ) : (
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
                    onDragStart={handleDragStart}
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                  />
                ))
              )}
            </ContentBox>
          )}
        </PageSurface>
      </PageRoot>
    </>
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
      <Typography
        sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary', flexShrink: 0 }}
      >
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
      title={<DragGuideContent />}
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

function DragGuideContent() {
  return (
    <Typography sx={{ fontSize: '0.75rem', fontWeight: 600 }}>
      드래그하여 서열 변경
    </Typography>
  );
}
