import { describe, expect, it } from 'vitest';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { DATA_SCOPED_MENUS, supportsDataScope } from './menuPermissionMatrixConfig';

describe('menuPermissionMatrixConfig', () => {
  it('실제 행 범위를 적용하는 메뉴만 데이터 범위 설정을 허용한다', () => {
    expect([...DATA_SCOPED_MENUS]).toEqual([
      MENU_CODE.SALES_CUSTOMERS,
      MENU_CODE.CONTRACTS,
    ]);
    expect(supportsDataScope(MENU_CODE.CUSTOMERS)).toBe(false);
    expect(supportsDataScope(MENU_CODE.EMPLOYEES)).toBe(false);
    expect(supportsDataScope(MENU_CODE.EQUIPMENTS)).toBe(false);
    expect(supportsDataScope(MENU_CODE.AFTER_SERVICES)).toBe(false);
  });
});
