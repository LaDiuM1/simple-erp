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
 * 셀 콘텐츠를 **컬럼 폭 (table-layout: fixed)** 에 맞춰 ellipsis 로 자르고, hover 시 Tooltip 으로 전체 텍스트 노출.
 * - width: 100% — 셀 폭을 그대로 채워 컬럼 폭이 곧 잘림 임계점이 된다.
 * - 직속 자식 (예: <Typography>) 도 ellipsis 가 적용되도록 cascade — block 컴포넌트로 감싼 셀 내용이 잘리지 않는 회귀 방지.
 * - 자기 자신 + 모든 descendant 를 검사해 overflow 여부 판단 — 자식이 자체 ellipsis 로 잘릴 때 부모는 overflow 미발생이라 직접 검사 필요.
 */
export default function TruncatedCellContent({ tooltip, align, children }: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const [overflow, setOverflow] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const update = () => {
      if (el.scrollWidth > el.clientWidth + 1) {
        setOverflow(true);
        return;
      }
      const all = el.querySelectorAll<HTMLElement>('*');
      for (const node of all) {
        if (node.scrollWidth > node.clientWidth + 1) {
          setOverflow(true);
          return;
        }
      }
      setOverflow(false);
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

const TruncatedDiv = styled('div', {
  shouldForwardProp: (prop) => prop !== '$align',
})<{ $align?: 'left' | 'center' | 'right' }>(({ $align }) => ({
  display: 'block',
  width: '100%',
  minWidth: 0,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  textAlign: $align ?? 'left',
  '& > *': {
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    maxWidth: '100%',
  },
}));
