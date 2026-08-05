import { describe, expect, it } from 'vitest';
import { employeeListColumns } from '@/features/employee/config/employeeListConfig';
import { computeResponsiveColumnLayout } from './utils';

describe('computeResponsiveColumnLayout', () => {
  it('넓은 화면에서도 고정 폭을 축소하지 않고 남는 폭만 flex 비율로 배분한다', () => {
    const layout = computeResponsiveColumnLayout(
      [{ width: 130 }, { flex: 1 }, { flex: 2 }, { width: 112 }],
      { viewportWidth: 1_600, reservedWidth: 64, minTableWidth: 1_008 },
    );

    expect(layout.tableWidth).toBe(1_600);
    expect(layout.columnWidths[0]).toBe(130);
    expect(layout.columnWidths[3]).toBe(112);
    expect(layout.columnWidths[2]).toBeCloseTo(layout.columnWidths[1] * 2);
    expect(layout.columnWidths.reduce((sum, width) => sum + width, 64)).toBeCloseTo(1_600);
  });

  it('좁은 화면에서는 컬럼을 찌그러뜨리지 않고 최소 테이블 폭을 늘린다', () => {
    const layout = computeResponsiveColumnLayout(
      [{ width: 130 }, { flex: 1.5 }, { flex: 1 }, { width: 112 }],
      { viewportWidth: 500, reservedWidth: 64, flexUnitWidth: 120 },
    );

    expect(layout.tableWidth).toBe(606); // 64 + 130 + 112 + (2.5 * 120)
    expect(layout.columnWidths).toEqual([130, 180, 120, 112]);
  });

  it('고정 컬럼만 있는 표는 선언한 정확한 폭을 확대하지 않는다', () => {
    const layout = computeResponsiveColumnLayout(
      [{ width: 100 }, { width: 300 }],
      { viewportWidth: 1_000, reservedWidth: 100, minTableWidth: 1_008 },
    );

    expect(layout.tableWidth).toBe(500);
    expect(layout.columnWidths).toEqual([100, 300]);
  });

  it('기준 데스크톱 폭에서는 직원 목록의 모든 열을 가로 스크롤 없이 배치한다', () => {
    const layout = computeResponsiveColumnLayout(
      employeeListColumns,
      { viewportWidth: 1_360, reservedWidth: 64, minTableWidth: 1_008 },
    );

    expect(layout.tableWidth).toBe(1_360);
    expect(layout.columnWidths.slice(-3)).toEqual([130, 130, 100]);
  });
});
