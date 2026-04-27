import { useEffect, useState, type RefObject } from 'react';

export interface FillRowHeightOptions {
  /** 한 행의 최소 높이 (px). 이 미만으로는 줄지 않으며, 부족 시 컨테이너가 자연 스크롤. */
  minHeight?: number;
  /** 한 행의 최대 높이 (px). 컨테이너가 매우 클 때 행이 무한히 커지지 않도록 상한. 미지정 시 무제한. */
  maxHeight?: number;
  /**
   * 안전 마진 (px). available 에서 추가로 빼서 micro-scroll 방지.
   * 기본 4 — 행마다 누적되는 1px border 와 sub-pixel rounding 흡수용.
   */
  safetyPx?: number;
}

const DEFAULT_MIN_HEIGHT = 38;
const DEFAULT_SAFETY_PX = 4;

/**
 * 스크롤 영역 높이를 행 수로 나눠 행 1개의 픽셀 높이를 반환.
 * - 화면이 커질수록 행도 같이 커짐 (사용 가능 공간 분할)
 * - minHeight 미만이면 minHeight 로 클램프 → 컨테이너가 자연 스크롤
 * - maxHeight 지정 시 그 이상으로 안 커짐
 * - rowCount 가 0 이면 minHeight 반환 (의미 없는 division 방지)
 *
 * 헤더 높이는 scrollArea 내부의 `thead` 를 실제 측정 (하드코딩 시 누적 오차로 micro-scroll 발생).
 * ResizeObserver 로 컨테이너 + 헤더 양쪽을 관찰해 사이즈 변화에 따라 자동 갱신.
 */
export function useFillRowHeight(
  scrollAreaRef: RefObject<HTMLElement | null>,
  rowCount: number,
  options: FillRowHeightOptions = {},
): number {
  const minHeight = options.minHeight ?? DEFAULT_MIN_HEIGHT;
  const maxHeight = options.maxHeight ?? Infinity;
  const safetyPx = options.safetyPx ?? DEFAULT_SAFETY_PX;
  const [height, setHeight] = useState(minHeight);

  useEffect(() => {
    const el = scrollAreaRef.current;
    if (!el || rowCount <= 0) {
      setHeight(minHeight);
      return;
    }

    const measure = () => {
      const head = el.querySelector<HTMLElement>('thead');
      const headerH = head?.getBoundingClientRect().height ?? 0;
      const avail = el.clientHeight - headerH - safetyPx;
      const ideal = Math.floor(avail / rowCount);
      setHeight(Math.min(maxHeight, Math.max(minHeight, ideal)));
    };

    measure();
    const observer = new ResizeObserver(measure);
    observer.observe(el);
    const head = el.querySelector<HTMLElement>('thead');
    if (head) observer.observe(head);
    return () => observer.disconnect();
  }, [scrollAreaRef, rowCount, minHeight, maxHeight, safetyPx]);

  return height;
}
