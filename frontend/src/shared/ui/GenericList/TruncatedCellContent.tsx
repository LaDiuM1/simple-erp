import { useEffect, useRef, useState, type ReactNode } from 'react';
import Tooltip from '@mui/material/Tooltip';
import { styled } from '@mui/material/styles';

interface Props {
  /** 잘림이 발생했을 때 노출할 툴팁 텍스트. 미지정/빈 문자열이면 잘려도 툴팁 미노출. */
  tooltip?: string;
  align?: 'left' | 'center' | 'right';
  children: ReactNode;
}

/**
 * 셀 콘텐츠가 셀 폭을 초과하면 ellipsis 로 자르고, hover 시 Tooltip 으로 전체 텍스트 노출.
 * - 항상 Tooltip 으로 wrap 하고 잘림 없을 때만 비활성화 → DOM 트리가 안 바뀌므로 ref/observer 안정.
 * - ResizeObserver 가 셀 폭 / 콘텐츠 변화를 감지해 overflow 상태 갱신.
 */
export default function TruncatedCellContent({ tooltip, align, children }: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const [overflow, setOverflow] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const update = () => {
      setOverflow(el.scrollWidth > el.clientWidth + 1);
    };
    update();
    const observer = new ResizeObserver(update);
    observer.observe(el);
    return () => observer.disconnect();
  }, [children]);

  const showTooltip = !!tooltip && overflow;

  return (
    <Tooltip
      title={tooltip ?? ''}
      arrow
      placement="top"
      disableHoverListener={!showTooltip}
      disableFocusListener={!showTooltip}
      disableTouchListener={!showTooltip}
    >
      <TruncatedDiv ref={ref} $align={align}>
        {children}
      </TruncatedDiv>
    </Tooltip>
  );
}

/**
 * table-layout: auto 환경에서:
 * - 짧은 content → 자연 너비 (셀이 content 만큼 자동 확장)
 * - 긴 content → max-width 에서 cap + ellipsis + 툴팁으로 전체 노출
 * MAX_CELL_WIDTH 이 컬럼별 상한 역할 — 너무 길어도 한 컬럼이 무한히 늘어나지 않음.
 */
const MAX_CELL_WIDTH = '20rem';

const TruncatedDiv = styled('div', {
  shouldForwardProp: (prop) => prop !== '$align',
})<{ $align?: 'left' | 'center' | 'right' }>(({ $align }) => ({
  display: 'inline-block',
  maxWidth: MAX_CELL_WIDTH,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  textAlign: $align ?? 'left',
  verticalAlign: 'middle',
}));
