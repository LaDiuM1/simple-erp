import { describe, expect, it } from 'vitest';
import { afterServiceListColumns } from './afterServiceListConfig';
import type { AfterServiceSummary } from '@/features/afterService/types';

describe('afterServiceListColumns', () => {
  it('말줄임되는 설비 열에서 모델명과 시리얼 전체를 tooltip으로 복구한다', () => {
    const equipmentColumn = afterServiceListColumns.find((column) => column.key === 'equipment');
    const row = {
      equipmentId: 12,
      equipmentModelName: 'ALW-06-FLEX',
      equipmentSerialNo: 'OGS-25-0001',
    } as AfterServiceSummary;

    expect(equipmentColumn?.tooltip?.(row, { filters: {} })).toBe(
      'ALW-06-FLEX (OGS-25-0001)',
    );
  });
});
