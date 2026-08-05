import type { FilterOption } from './types';

/**
 * SelectFilterItem.mapOptions 에 그대로 꽂아 쓰는 공용 매퍼.
 * 서버가 돌려주는 `{ id: number; name: string }[]` 배열을 FilterOption[] 으로 변환한다.
 * 부서/직책/권한 등 참조 테이블 드롭다운에서 공통으로 사용.
 */
export const mapIdName = (data: unknown): FilterOption[] =>
  (data as { id: number; name: string }[]).map((x) => ({ value: x.id, label: x.name }));

export interface ResponsiveColumnLayout {
  /** colgroup 에 적용할 도메인 컬럼별 실제 폭(px). */
  columnWidths: number[];
  /** 구조 컬럼(No, checkbox 등)을 포함한 최종 테이블 폭(px). */
  tableWidth: number;
}

interface ResponsiveColumnLayoutOptions {
  /** 테이블을 담는 스크롤 영역의 현재 폭(px). */
  viewportWidth: number;
  /** No, checkbox 등 도메인 컬럼 밖의 고정 폭 합계(px). */
  reservedWidth?: number;
  /** 화면 종류별 최소 테이블 폭(px). */
  minTableWidth?: number;
  /** flex 1당 확보할 최소 폭(px). */
  flexUnitWidth?: number;
}

const DEFAULT_FLEX_UNIT_WIDTH = 104;

/** 0 이하의 flex는 레이아웃을 무너뜨리므로 기본 비율 1로 정규화한다. */
function normalizedFlex(column: { flex?: number }): number {
  return column.flex != null && column.flex > 0 ? column.flex : 1;
}

/**
 * 고정 폭 컬럼을 줄이지 않고, 남은 공간만 flex 컬럼에 배분하는 데스크탑 테이블 레이아웃.
 *
 * fixed table에서 `px` 컬럼과 합계 100%인 flex 컬럼을 함께 선언하면 브라우저가 전체 폭을
 * 맞추기 위해 px 컬럼까지 비례 축소한다. 접수번호나 날짜가 넓은 화면에서도 잘리던 원인이다.
 * 이 함수는 컨테이너 실측값으로 최종 폭을 px 단위로 확정해 그 축소를 차단한다. 화면이 좁으면
 * flex 컬럼의 최소 폭을 보존한 테이블 자체가 넓어지고, 상위 스크롤 영역이 가로 스크롤을 맡는다.
 */
export function computeResponsiveColumnLayout(
  columns: ReadonlyArray<{ width?: number; flex?: number }>,
  {
    viewportWidth,
    reservedWidth = 0,
    minTableWidth = 0,
    flexUnitWidth = DEFAULT_FLEX_UNIT_WIDTH,
  }: ResponsiveColumnLayoutOptions,
): ResponsiveColumnLayout {
  const fixedWidth = columns.reduce(
    (sum, column) => sum + (column.width != null ? column.width : 0),
    0,
  );
  const flexibleColumns = columns.filter((column) => column.width == null);
  const totalFlex = flexibleColumns.reduce((sum, column) => sum + normalizedFlex(column), 0);
  const minimumDomainWidth = fixedWidth + totalFlex * flexUnitWidth;
  const minimumTableWidth = Math.max(minTableWidth, reservedWidth + minimumDomainWidth);
  const tableWidth = Math.max(Math.floor(viewportWidth), minimumTableWidth);

  if (totalFlex > 0) {
    const flexibleWidth = Math.max(0, tableWidth - reservedWidth - fixedWidth);
    return {
      tableWidth,
      columnWidths: columns.map((column) =>
        column.width != null
          ? column.width
          : flexibleWidth * (normalizedFlex(column) / totalFlex)),
    };
  }

  // 모든 컬럼이 고정 폭이면 선언한 계약을 그대로 지키고 남는 공간은 표 바깥에 둔다.
  return {
    tableWidth: reservedWidth + fixedWidth,
    columnWidths: columns.map((column) => column.width ?? 0),
  };
}

/**
 * 컨테이너 실측을 하지 않는 소형 정적 표·모달용 colgroup 폭 계산.
 * 고정 폭이 섞인 전체 화면 목록은 브라우저가 px 컬럼까지 줄일 수 있으므로
 * `computeResponsiveColumnLayout`을 사용한다.
 */
export function computeColumnWidths(
  columns: ReadonlyArray<{ width?: number; flex?: number }>,
): string[] {
  const totalFlex = columns.reduce((sum, c) => (c.width != null ? sum : sum + (c.flex ?? 1)), 0);
  return columns.map((col) => {
    if (col.width != null) return `${col.width}px`;
    const ratio = totalFlex > 0 ? (col.flex ?? 1) / totalFlex : 0;
    return `${(ratio * 100).toFixed(4)}%`;
  });
}
