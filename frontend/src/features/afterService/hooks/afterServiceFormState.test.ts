import { describe, expect, it, vi } from 'vitest';
import { changeAfterServiceCustomer } from './afterServiceFormState';

describe('changeAfterServiceCustomer', () => {
  it('고객사 변경 시 기존 설비 id와 표시값을 함께 제거한다', () => {
    const update = vi.fn();

    changeAfterServiceCustomer(update, '48', '새 고객사');

    expect(update.mock.calls).toEqual([
      ['customerId', '48'],
      ['customerName', '새 고객사'],
      ['equipmentId', ''],
      ['equipmentLabel', ''],
    ]);
  });
});
