import type { ReactNode } from 'react';
import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import type { CommonSearchSelectedItem } from './types';

interface Props {
  items: CommonSearchSelectedItem[];
  onRemove: (id: number) => void;
  /** 비어있을 때 노출할 안내 문구. 미지정 시 "선택된 항목이 없습니다." */
  emptyHint?: string;
  /** 칩 커스텀 렌더링 — 호출자가 외부 상태 (예: 날짜) 와 결합한 칩을 제공할 때 사용. */
  renderItem?: (item: CommonSearchSelectedItem, onRemove: () => void) => ReactNode;
}

/**
 * tray 모드 — 선택된 항목을 칩 리스트로 노출. 칩 X 클릭 시 onRemove(id) 호출.
 * 모달 본문 (테이블) 과 페이지네이션 사이에 끼어 들어가도록 디자인.
 */
export default function SelectionTray({ items, onRemove, emptyHint, renderItem }: Props) {
  return (
    <TrayRoot>
      <TrayHeader>
        <Typography sx={{ fontSize: '0.75rem', fontWeight: 600, color: 'text.secondary' }}>
          선택됨 ({items.length})
        </Typography>
      </TrayHeader>
      <TrayContent>
        {items.length === 0 ? (
          <Typography sx={{ fontSize: '0.8125rem', color: 'text.disabled' }}>
            {emptyHint ?? '선택된 항목이 없습니다 — 위 행을 클릭해 추가하세요.'}
          </Typography>
        ) : renderItem ? (
          items.map((item) => (
            <span key={item.id}>{renderItem(item, () => onRemove(item.id))}</span>
          ))
        ) : (
          items.map((item) => (
            <Chip
              key={item.id}
              size="small"
              label={item.label}
              onDelete={() => onRemove(item.id)}
              sx={{ fontSize: '0.75rem' }}
            />
          ))
        )}
      </TrayContent>
    </TrayRoot>
  );
}

const TrayRoot = styled(Box)(({ theme }) => ({
  flexShrink: 0,
  borderTop: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.default,
}));

const TrayHeader = styled(Box)({
  padding: '0.5rem 1rem 0.25rem',
});

const TrayContent = styled(Box)({
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.375rem',
  padding: '0 1rem 0.75rem',
  minHeight: '2rem',
  alignItems: 'center',
});
