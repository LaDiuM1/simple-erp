/**
 * 시스템 메뉴 코드 (BE 의 io.github.ladium1.erp.global.menu.Menu enum 과 동기).
 * 메뉴 추가 시 BE Menu + 여기 + MENU_PATH + MENU_CONFIG 4곳을 함께 갱신.
 */
export const MENU_CODE = {
  EMPLOYEES: 'EMPLOYEES',
  DEPARTMENTS: 'DEPARTMENTS',
  POSITIONS: 'POSITIONS',
  CODE_RULES: 'CODE_RULES',
} as const;

export type MenuCode = (typeof MENU_CODE)[keyof typeof MENU_CODE];

/** 각 메뉴의 라우트 경로. 페이지 / 권한 게이트가 공통으로 참조. */
export const MENU_PATH = {
  [MENU_CODE.EMPLOYEES]: '/employees',
  [MENU_CODE.DEPARTMENTS]: '/departments',
  [MENU_CODE.POSITIONS]: '/positions',
  [MENU_CODE.CODE_RULES]: '/code-rules',
} as const satisfies Record<MenuCode, string>;

export interface MenuConfig {
  code: string;
  name: string;
  to?: string;
  children?: MenuConfig[];
}

export const MENU_CONFIG: MenuConfig[] = [
  {
    code: 'MDM',
    name: '기준 정보 관리',
    children: [
      { code: MENU_CODE.DEPARTMENTS, name: '부서 관리', to: MENU_PATH.DEPARTMENTS },
      { code: MENU_CODE.POSITIONS, name: '직책 관리', to: MENU_PATH.POSITIONS },
      { code: MENU_CODE.EMPLOYEES, name: '직원 관리', to: MENU_PATH.EMPLOYEES },
    ],
  },
  {
    code: 'SYSTEM',
    name: '시스템 설정',
    children: [
      { code: MENU_CODE.CODE_RULES, name: '코드 채번 규칙', to: MENU_PATH.CODE_RULES },
    ],
  },
];
