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
  delete?: string;
}

export interface ListApiConfig<TRow, TFilters extends object> {
  menuCode: string;

  useList: (params: TFilters & ListQueryParamsBase) => QueryState<TRow>;
  useDelete: UseDeleteMutation;
  /** 지정 시 리스트 툴바 우측에 엑셀 다운로드 버튼이 자동 노출 */
  useExcel?: UseExcelDownload<TFilters>;

  rowKey: (row: TRow) => number;

  /** 행 수정 핸들러. 지정 시 행 수정 아이콘 노출. 수정 페이지로 navigate 용도. */
  onEdit?: (row: TRow) => void;

  /**
   * 행 전체 클릭 핸들러. 지정 시 행에 cursor pointer + hover 강조 + 클릭 시 호출.
   * 편집/삭제/체크박스 등 행 내부 액션은 자동으로 propagation 을 막아 행 클릭과 경쟁하지 않는다.
   * 일반적으로 상세 페이지 진입 (읽기 권한만으로 가능) 에 사용.
   */
  onRowClick?: (row: TRow) => void;

  /**
   * true 면 No 컬럼 왼쪽에 체크박스 열 노출 (canWrite 일 때만).
   * 사용 시 GenericList 의 selection prop 도 함께 전달해야 한다.
   */
  checkBox?: boolean;

  emptyMessage?: string;
  deleteConfirm?: DeleteConfirmMessages;
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
