import type { ReactNode } from 'react';
import type { PageResponse } from '@/shared/types/api';

export type SortDirection = 'asc' | 'desc';

export interface SortState {
  key: string;
  direction: SortDirection;
}

interface BaseColumn<TRow> {
  key: string;
  label: string;
  align?: 'left' | 'center' | 'right';
  width?: string | number;
  mobilePrimary?: boolean;
  hideOnMobile?: boolean;
  /**
   * true 면 셀에 ellipsis truncation + 잘림 시 Tooltip 미적용 (배지/칩 등 짧은 컴포넌트 셀 용도).
   * 기본 false — 모든 셀이 nowrap + 잘림 시 hover Tooltip 으로 전체 텍스트 노출.
   */
  noTruncate?: boolean;
  render?: (row: TRow) => ReactNode;
}

/**
 * Discriminated union:
 *   - 비정렬: sortable 생략 또는 false. sortDirection/defaultSort 작성 불가.
 *   - 정렬: sortable: true 면 sortDirection 필수. defaultSort 는 initial sort 지정용 boolean.
 */
export type ColumnConfig<TRow> = BaseColumn<TRow> &
  (
    | { sortable?: false; sortDirection?: never; defaultSort?: never }
    | { sortable: true; sortDirection: SortDirection; defaultSort?: boolean }
  );

export interface QueryState<T> {
  data?: PageResponse<T>;
  isFetching: boolean;
  isError: boolean;
  error?: unknown;
  refetch: () => void;
}

export interface FilterOption<V extends string | number = string | number> {
  value: V;
  label: string;
}

export interface SearchFilterItem {
  type: 'search';
  key: string;
  placeholder?: string;
}

export interface SelectFilterItem {
  type: 'select';
  key: string;
  label: string;
  options?: FilterOption[];
  useOptions?: () => { data?: unknown };
  mapOptions?: (data: unknown) => FilterOption[];
  minWidth?: number;
}

export interface CustomFilterItem {
  type: 'custom';
  key: string;
  render: (ctx: { value: unknown; onChange: (value: unknown) => void }) => ReactNode;
}

export type FilterConfig = SearchFilterItem | SelectFilterItem | CustomFilterItem;

export interface ListQueryParamsBase {
  page: number;
  size: number;
  sort: string;
}

export type UseDeleteMutation = () => readonly [
  (id: number) => { unwrap: () => Promise<unknown> },
  unknown,
];

/**
 * 일괄 삭제 mutation. 단건 useDelete 와 별개로, 여러 ID 를 한 번의 요청으로 처리.
 * 백엔드는 `DELETE /api/v1/{resource}` + body `[id, ...]` 패턴으로 통일.
 */
export type UseBulkDeleteMutation = () => readonly [
  (ids: number[]) => { unwrap: () => Promise<unknown> },
  unknown,
];

/**
 * 엑셀 다운로드 훅.
 * 인자: 필터 전체 + 현재 정렬 조건 (page/size 는 제외 — 필터링된 전체 데이터 내려받기).
 */
export type UseExcelDownload<TFilters> = () => (
  params: TFilters & { sort?: string },
) => unknown;

export interface DeleteConfirmMessages {
  title: string;
  /** message 내 `{no}` 가 있으면 삭제 대상 행의 No (글로벌 데이터 순번) 로 치환된다. */
  message: string;
}

/** 목록 화면에서 성공 이벤트 발생 시 스낵바로 노출할 메시지 오버라이드. */
export interface ListSuccessMessages {
  /** 단건 삭제 성공 메시지 (모바일 카드 행 삭제). */
  delete?: string;
  /**
   * 일괄 삭제 성공 메시지. `{count}` 토큰이 있으면 삭제 건수로 치환.
   * 미지정 시 `${count}건이 삭제되었습니다.` 사용.
   */
  bulkDelete?: string;
}

export interface ListApiConfig<TRow, TFilters extends object> {
  menuCode: string;

  useList: (params: TFilters & ListQueryParamsBase) => QueryState<TRow>;
  /**
   * 단건 행 삭제 mutation — 모바일 카드의 행 삭제 아이콘 전용.
   * 데스크탑에서는 사용하지 않는다 (체크박스 + 일괄 삭제로 통일).
   */
  useDelete?: UseDeleteMutation;
  /**
   * 일괄 삭제 mutation — 데스크탑 체크박스 선택 후 [선택 N건 삭제] 버튼 동작.
   * 지정 시 데스크탑 테이블에 체크박스 열이 자동 노출되고 필터바 우측에 일괄 삭제 버튼이 표시된다.
   */
  useBulkDelete?: UseBulkDeleteMutation;
  /** 지정 시 리스트 툴바 우측에 엑셀 다운로드 버튼이 자동 노출 */
  useExcel?: UseExcelDownload<TFilters>;

  rowKey: (row: TRow) => number;

  /**
   * 모바일 카드의 행 수정 아이콘 핸들러. 지정 시 카드 우상단에 수정 아이콘 노출.
   * 데스크탑에서는 행 클릭 → 상세 페이지 → [수정] 버튼 흐름으로 통일되므로 사용되지 않는다.
   */
  onEdit?: (row: TRow) => void;

  /**
   * 행 전체 클릭 핸들러. 지정 시 행에 cursor pointer + hover 강조 + 클릭 시 호출.
   * 모바일 카드의 수정/삭제 아이콘은 자동으로 propagation 을 막아 카드 클릭과 경쟁하지 않는다.
   * 일반적으로 상세 페이지 진입 (읽기 권한만으로 가능) 에 사용.
   */
  onRowClick?: (row: TRow) => void;

  emptyMessage?: string;
  /** 모바일 단건 삭제 확인 모달 메시지. `{no}` 가 있으면 행 No 로 치환. */
  deleteConfirm?: DeleteConfirmMessages;
  /** 데스크탑 일괄 삭제 확인 모달 메시지. `{count}` 가 있으면 선택 건수로 치환. */
  bulkDeleteConfirm?: DeleteConfirmMessages;
  /** 삭제 성공 토스트 메시지 오버라이드 */
  successMessages?: ListSuccessMessages;
  pageSize?: number;
}

export interface ListState<TFilters> {
  filters: TFilters;
  updateFilter: <K extends keyof TFilters>(key: K, value: TFilters[K]) => void;
  resetFilters: () => void;

  page: number;
  setPage: (page: number) => void;
  pageSize: number;

  sort: SortState;
  setSort: (sort: SortState) => void;

  queryParams: TFilters & ListQueryParamsBase;
}
