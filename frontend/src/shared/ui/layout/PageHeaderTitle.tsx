import { useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

interface OutletCtx {
  setTitleOverride: (title: string | null) => void;
}

/**
 * 페이지가 동적 타이틀을 헤더에 push 한다 (예: "고객사 코드 수정").
 * 미사용 페이지는 pathname 기반 fallback (`pageTitles.ts`) 이 그대로 적용.
 * unmount 시 자동 cleanup.
 */
export default function PageHeaderTitle({ children }: { children: string }) {
  const ctx = useOutletContext<OutletCtx | null>();
  useEffect(() => {
    if (!ctx) return;
    ctx.setTitleOverride(children);
    return () => ctx.setTitleOverride(null);
  }, [children, ctx]);
  return null;
}
