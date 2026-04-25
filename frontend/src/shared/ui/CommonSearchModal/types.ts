import type {
  ColumnConfig,
  FilterConfig,
  ListQueryParamsBase,
  QueryState,
} from '@/shared/ui/GenericList';

/** 모달이 부모로 반환하는 단일 항목. id 는 폼 값으로, label 은 표시용으로 사용. */
export interface CommonSearchSelectedItem {
  id: number;
  label: string;
}

/** 모달이 검색할 도메인의 데이터 소스 + 행 → (id, label) 매핑. */
export interface CommonSearchModalApi<TRow, TFilters extends object> {
  /** 페이징/정렬/필터 파라미터로 호출되는 RTK Query 훅. */
  useList: (params: TFilters & ListQueryParamsBase) => QueryState<TRow>;
  /** 행 → 폼으로 넘어갈 id 값. */
  rowKey: (row: TRow) => number;
  /** 행 → 트리거에 표시될 라벨 (선택 결과로 부모에 함께 반환됨). */
  rowLabel: (row: TRow) => string;
  /** 모달 내 페이지 사이즈. default 5 — 좁은 모달에 맞춤. */
  pageSize?: number;
}

export interface CommonSearchModalProps<TRow, TFilters extends object> {
  open: boolean;
  onClose: () => void;
  title: string;
  /** true 면 다중 선택 (체크박스 다중 토글), false 면 단일 선택 (한 행만 유지). */
  multiple?: boolean;
  api: CommonSearchModalApi<TRow, TFilters>;
  /** 리스트 필터바 (search / select / custom) — GenericList 와 동일 구성. */
  searchFilter: FilterConfig[];
  /** 행에 표시할 컬럼 — GenericList 와 동일 구성 (sortable 등 일부 표시는 모달에서 미사용). */
  column: ColumnConfig<TRow>[];
  /**
   * 확인 클릭 시 부모로 선택 결과 전달.
   * 단일 선택이면 길이 0 또는 1, 다중 선택이면 0개 이상.
   */
  onSelect: (selected: CommonSearchSelectedItem[]) => void;
  /** 모달 열릴 때 미리 체크해둘 항목 (수정 폼에서 현재 값 미리 표시 등). */
  initialSelected?: CommonSearchSelectedItem[];
  /** 결과에서 제외할 id 목록 (예: 부서 자기 자신을 상위 부서로 못 고르도록). */
  excludeIds?: number[];
  /** 빈 결과 메시지 오버라이드. */
  emptyMessage?: string;
}
