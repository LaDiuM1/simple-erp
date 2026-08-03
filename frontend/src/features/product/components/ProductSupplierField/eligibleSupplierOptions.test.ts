import { describe, expect, it } from 'vitest';
import type { SupplierInfo } from '@/features/reference/types';
import { eligibleSupplierOptions } from './eligibleSupplierOptions';

const suppliers: SupplierInfo[] = [
  { id: 1, name: 'Active', nameKo: null, country: null, active: true },
  { id: 2, name: 'Inactive', nameKo: null, country: null, active: false },
];

describe('eligibleSupplierOptions', () => {
  it('신규 제품에는 사용 중인 공급사만 노출한다', () => {
    expect(eligibleSupplierOptions(suppliers, null).map((supplier) => supplier.id)).toEqual([1]);
  });

  it('수정 중인 제품의 기존 비활성 공급사는 선택값으로 유지한다', () => {
    expect(eligibleSupplierOptions(suppliers, 2).map((supplier) => supplier.id)).toEqual([1, 2]);
  });
});
