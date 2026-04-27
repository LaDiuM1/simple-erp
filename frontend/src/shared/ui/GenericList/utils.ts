import type { ColumnConfig, FilterOption } from './types';

/**
 * SelectFilterItem.mapOptions 에 그대로 꽂아 쓰는 공용 매퍼.
 * 서버가 돌려주는 `{ id: number; name: string }[]` 배열을 FilterOption[] 으로 변환한다.
 * 부서/직책/권한 등 참조 테이블 드롭다운에서 공통으로 사용.
 */
export const mapIdName = (data: unknown): FilterOption[] =>
  (data as { id: number; name: string }[]).map((x) => ({ value: x.id, label: x.name }));

/**
 * colgroup 의 각 도메인 컬럼에 적용할 width 를 계산.
 * - col.width (px) 지정 시 그 폭 그대로 (`Npx`).
 * - 미지정 시 col.flex (기본 1) 비율을 정규화한 percentage 반환 — flex 컬럼들의 합이 100% 가 되도록.
 *   브라우저의 fixed-table-layout 알고리즘이 fixed-px 컬럼 폭을 먼저 빼고 남은 공간을
 *   percentage 컬럼들에 비율대로 분배한다.
 *
 * `<col>` 요소에 `calc((100% - Npx) * ratio)` 를 주면 Chromium 이 균등 분배로 fallback 하는
 * 버그가 있어 percentage 로 표현. fixed-px 공간 차감은 브라우저 layout 알고리즘이 자동 처리.
 *
 * table-layout: fixed 와 함께 사용 — colgroup width 가 모든 행의 컬럼 폭을 고정한다.
 */
export function computeColumnWidths<TRow>(columns: ColumnConfig<TRow>[]): string[] {
  const totalFlex = columns.reduce((sum, c) => (c.width != null ? sum : sum + (c.flex ?? 1)), 0);
  return columns.map((col) => {
    if (col.width != null) return `${col.width}px`;
    const ratio = totalFlex > 0 ? (col.flex ?? 1) / totalFlex : 0;
    return `${(ratio * 100).toFixed(4)}%`;
  });
}
