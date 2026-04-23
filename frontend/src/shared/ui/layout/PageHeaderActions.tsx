import type { ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { useOutletContext } from 'react-router-dom';

interface OutletCtx {
  pageHeaderActionsNode: HTMLElement | null;
}

/**
 * 페이지에서 PageHeader 우측 액션 영역으로 React Portal을 통해 노드를 주입한다.
 * AppLayout에서 outlet context로 actions slot DOM ref를 내려준다.
 */
export default function PageHeaderActions({ children }: { children: ReactNode }) {
  const ctx = useOutletContext<OutletCtx | null>();
  const node = ctx?.pageHeaderActionsNode;
  if (!node) return null;
  return createPortal(children, node);
}
