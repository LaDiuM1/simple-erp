import type { ReactNode } from 'react';
import { FilterBarArea } from './GenericList.styles';

interface Props {
  /** false면 행과 구분선까지 렌더하지 않는다. */
  visible?: boolean;
  children: ReactNode;
}

/** 목록 surface의 선택적 상단 행. 검색 필터와 breadcrumb가 같은 여백·구분선을 공유한다. */
export default function ListSurfaceToolbar({ visible = true, children }: Props) {
  if (!visible) return null;
  return <FilterBarArea>{children}</FilterBarArea>;
}
