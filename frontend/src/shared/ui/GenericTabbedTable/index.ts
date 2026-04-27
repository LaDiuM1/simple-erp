export { default } from './GenericTabbedTable';
export { tabbedTab } from './types';
export type {
  AnyTabbedTab,
  TabbedTab,
  TabbedTableColumn,
  TabHookResult,
} from './types';

/**
 * 탭 우측 슬롯 표준 액션 버튼 — PageHeader 액션과 동일 톤, 살짝 낮은 height (30 vs 34).
 * `TabActionButton` = secondary outline (cancel/secondary 톤),
 * `TabPrimaryActionButton` = primary filled (create/save 톤).
 */
export { TabActionButton, TabPrimaryActionButton } from './GenericTabbedTable.styles';

/**
 * 탭 셀 공용 primitives — truncate / nowrap / status text / row actions / inline link / formatDateTime.
 * 탭 hook 들이 일관된 시각 톤으로 컬럼을 조립하도록.
 */
export {
  TruncatedSpan,
  NowrapText,
  StatusText,
  RowActions,
  InlineLinkButton,
  formatDateTime,
} from './cells';
export type { StatusTone } from './cells';
