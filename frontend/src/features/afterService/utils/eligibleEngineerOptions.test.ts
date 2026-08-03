import { describe, expect, it } from 'vitest';
import type { Engineer } from '@/features/afterService/types';
import { eligibleEngineerOptions } from './eligibleEngineerOptions';

const engineers: Engineer[] = [
  {
    id: 1, name: 'Active', type: 'INTERNAL', affiliation: null,
    phone: null, employeeId: 1, employeeName: 'Active', active: true,
  },
  {
    id: 2, name: 'Inactive', type: 'OUTSOURCED', affiliation: null,
    phone: null, employeeId: null, employeeName: null, active: false,
  },
];

describe('eligibleEngineerOptions', () => {
  it('새 AS 기록에는 사용 중인 엔지니어만 노출한다', () => {
    expect(eligibleEngineerOptions(engineers, null).map((engineer) => engineer.id)).toEqual([1]);
  });

  it('수정 중인 기록의 기존 비활성 엔지니어는 선택값으로 유지한다', () => {
    expect(eligibleEngineerOptions(engineers, '2').map((engineer) => engineer.id)).toEqual([1, 2]);
  });
});
