import { useLayoutEffect, useState, type RefObject } from 'react';

/** ResizeObserver로 스크롤 컨테이너의 실제 내부 폭을 추적한다. */
export function useElementWidth(ref: RefObject<HTMLElement | null>): number {
  const [width, setWidth] = useState(0);

  useLayoutEffect(() => {
    const element = ref.current;
    if (!element) return;

    const update = () => setWidth(element.clientWidth);
    update();

    const observer = new ResizeObserver(update);
    observer.observe(element);
    return () => observer.disconnect();
  }, [ref]);

  return width;
}
