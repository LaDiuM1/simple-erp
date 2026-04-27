export { default } from './GenericList';
export type { GenericListProps } from './GenericList';

export { mapIdName } from './utils';

/** 내부 building blocks — CommonSearchModal 등 다른 합성 위젯에서 재사용. */
export { default as ListSearchFilter } from './ListSearchFilter';
export { default as ListPagination } from './ListPagination';
export { useListState } from './useListState';

/**
 * 테이블 styled primitives — GenericList 와 동일한 시각 톤을 페이지가 직접 조립할 때 사용.
 * 페이징/검색/삭제가 모두 불필요한 단순 목록 (예: 코드 채번 규칙) 에서 import 한다.
 */
export {
  BodyCell,
  BodyRow,
  EmptyStateContainer,
  EmptyStateText,
  HeaderCell,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from './ListTable.styles';

export { ListRoot, ListSurface } from './GenericList.styles';

export type {
  ColumnConfig,
  CustomFilterItem,
  DeleteConfirmMessages,
  FilterConfig,
  FilterOption,
  ListApiConfig,
  ListQueryParamsBase,
  ListState,
  QueryState,
  SearchFilterItem,
  SelectFilterItem,
  SortDirection,
  SortState,
  UseBulkDeleteMutation,
  UseDeleteMutation,
  UseExcelDownload,
} from './types';
