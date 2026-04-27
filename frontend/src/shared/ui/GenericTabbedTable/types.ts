import type { ReactNode } from 'react';
import type { SxProps, Theme } from '@mui/material/styles';

export interface TabbedTableColumn<T> {
  /** 컬럼 식별자 — react key 와 정렬에 사용. */
  key: string;
  header: string;
  /** 셀 내용 정렬 (header / body 동시 적용). */
  align?: 'left' | 'right' | 'center';
  /** 명시 폭 — 미설정 시 콘텐츠 자동 폭. */
  width?: number | string;
  render: (row: T) => ReactNode;
}

export interface TabbedTab<T> {
  /** 탭 식별자 — 활성 탭 추적용. */
  key: string;
  label: string;
  /** 라벨 우측 muted 톤 suffix (예: 3, "1 / 2"). 0 도 노출. */
  count?: ReactNode;
  columns: TabbedTableColumn<T>[];
  rows: T[];
  /** 데이터 없을 때 본문 메시지 — 미설정 시 기본 문구. */
  emptyMessage?: string;
  /** 활성 탭일 때 우측 슬롯에 노출 (등록 버튼 등). */
  rightSlot?: ReactNode;
  rowKey?: (row: T) => string | number;
  rowSx?: (row: T) => SxProps<Theme>;
  /** 행 클릭 시 핸들러 — 설정 시 cursor pointer + hover 효과로 클릭 가능 표시. */
  onRowClick?: (row: T) => void;
}

/**
 * 탭 배열은 다양한 row 타입을 섞어 담으므로 컴포넌트 내부에서 unknown 으로 다룬다.
 * 도메인 hook 은 자기 타입의 `TabbedTab<T>` 을 만든 뒤 `tabbedTab(...)` 으로 widen.
 */
export type AnyTabbedTab = TabbedTab<unknown>;

export function tabbedTab<T>(t: TabbedTab<T>): AnyTabbedTab {
  return t as unknown as AnyTabbedTab;
}

/**
 * 탭 hook 의 표준 반환 형태. 데이터 로딩은 outer 가 분기 처리해 hook 은 보장된 데이터를 받는 정책 (CLAUDE.md).
 * `modals` 는 hook 이 자기 모달 (CRUD / Detail) 을 owner 로 들고 있을 때 ReactNode, 그 외엔 null.
 */
export interface TabHookResult {
  tab: AnyTabbedTab;
  modals: ReactNode | null;
}
