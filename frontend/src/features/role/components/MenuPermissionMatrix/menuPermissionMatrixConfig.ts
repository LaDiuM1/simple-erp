import { MENU_CODE, type MenuCode } from '@/shared/config/menuConfig';

/** 매트릭스 행 순서 — MENU_CODE 선언 순서를 따른다. */
export const MATRIX_MENUS: MenuCode[] = Object.values(MENU_CODE);

/** 서버가 행 단위 데이터 범위를 실제 조회에 적용하는 메뉴. */
export const DATA_SCOPED_MENUS: ReadonlySet<MenuCode> = new Set([
  MENU_CODE.SALES_CUSTOMERS,
  MENU_CODE.CONTRACTS,
]);

export const supportsDataScope = (menu: MenuCode) => DATA_SCOPED_MENUS.has(menu);
