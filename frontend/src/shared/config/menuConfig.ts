/**
 * 시스템 메뉴 코드 (BE 의 io.github.ladium1.erp.global.menu.Menu enum 과 동기).
 * 메뉴 추가 시 BE Menu + 여기 + MENU_PATH + MENU_CONFIG 4곳을 함께 갱신.
 */
export const MENU_CODE = {
  EMPLOYEES: 'EMPLOYEES',
  DEPARTMENTS: 'DEPARTMENTS',
  POSITIONS: 'POSITIONS',
  CUSTOMERS: 'CUSTOMERS',
  SUPPLIERS: 'SUPPLIERS',
  PRODUCTS: 'PRODUCTS',
  SALES_CONTACTS: 'SALES_CONTACTS',
  SALES_CUSTOMERS: 'SALES_CUSTOMERS',
  CONTRACTS: 'CONTRACTS',
  ROLES: 'ROLES',
  CODE_RULES: 'CODE_RULES',
  APPROVALS: 'APPROVALS',
  EXPENSES: 'EXPENSES',
  ATTENDANCE: 'ATTENDANCE',
  BOARDS: 'BOARDS',
  DRIVE: 'DRIVE',
} as const;

export type MenuCode = (typeof MENU_CODE)[keyof typeof MENU_CODE];

/** 각 메뉴의 라벨 — 매트릭스 등 BE Menu 라벨이 필요한 화면에서 사용. */
export const MENU_LABEL: Record<MenuCode, string> = {
  [MENU_CODE.EMPLOYEES]: '직원 관리',
  [MENU_CODE.DEPARTMENTS]: '부서 관리',
  [MENU_CODE.POSITIONS]: '직책 관리',
  [MENU_CODE.CUSTOMERS]: '고객사 관리',
  [MENU_CODE.SUPPLIERS]: '공급사 관리',
  [MENU_CODE.PRODUCTS]: '제품 모델 관리',
  [MENU_CODE.SALES_CONTACTS]: '영업 명부 관리',
  [MENU_CODE.SALES_CUSTOMERS]: '고객사 영업 관리',
  [MENU_CODE.CONTRACTS]: '계약 관리',
  [MENU_CODE.ROLES]: '권한 관리',
  [MENU_CODE.CODE_RULES]: '코드 채번 규칙',
  [MENU_CODE.APPROVALS]: '전자결재',
  [MENU_CODE.EXPENSES]: '경비 처리',
  [MENU_CODE.ATTENDANCE]: '근태 관리',
  [MENU_CODE.BOARDS]: '게시판',
  [MENU_CODE.DRIVE]: '드라이브',
};

/** 각 메뉴의 라우트 경로. 페이지 / 권한 게이트가 공통으로 참조. */
export const MENU_PATH = {
  [MENU_CODE.EMPLOYEES]: '/employees',
  [MENU_CODE.DEPARTMENTS]: '/departments',
  [MENU_CODE.POSITIONS]: '/positions',
  [MENU_CODE.CUSTOMERS]: '/customers',
  [MENU_CODE.SUPPLIERS]: '/suppliers',
  [MENU_CODE.PRODUCTS]: '/products',
  [MENU_CODE.SALES_CONTACTS]: '/sales-contacts',
  [MENU_CODE.SALES_CUSTOMERS]: '/sales-customers',
  [MENU_CODE.CONTRACTS]: '/contracts',
  [MENU_CODE.ROLES]: '/roles',
  [MENU_CODE.CODE_RULES]: '/code-rules',
  [MENU_CODE.APPROVALS]: '/approvals',
  [MENU_CODE.EXPENSES]: '/expenses',
  [MENU_CODE.ATTENDANCE]: '/attendance',
  [MENU_CODE.BOARDS]: '/boards',
  [MENU_CODE.DRIVE]: '/drive',
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
      { code: MENU_CODE.CUSTOMERS, name: '고객사 관리', to: MENU_PATH.CUSTOMERS },
      { code: MENU_CODE.SUPPLIERS, name: '공급사 관리', to: MENU_PATH.SUPPLIERS },
      { code: MENU_CODE.PRODUCTS, name: '제품 모델 관리', to: MENU_PATH.PRODUCTS },
      { code: MENU_CODE.SALES_CONTACTS, name: '영업 명부 관리', to: MENU_PATH.SALES_CONTACTS },
    ],
  },
  {
    code: 'SALES',
    name: '영업 관리',
    children: [
      { code: MENU_CODE.SALES_CUSTOMERS, name: '고객사 영업 관리', to: MENU_PATH.SALES_CUSTOMERS },
      { code: MENU_CODE.CONTRACTS, name: '계약 관리', to: MENU_PATH.CONTRACTS },
    ],
  },
  {
    code: 'GROUPWARE',
    name: '그룹웨어',
    children: [
      { code: MENU_CODE.APPROVALS, name: '전자결재', to: MENU_PATH.APPROVALS },
      { code: MENU_CODE.EXPENSES, name: '경비 처리', to: MENU_PATH.EXPENSES },
      { code: MENU_CODE.ATTENDANCE, name: '근태 관리', to: MENU_PATH.ATTENDANCE },
      { code: MENU_CODE.BOARDS, name: '게시판', to: MENU_PATH.BOARDS },
      { code: MENU_CODE.DRIVE, name: '드라이브', to: MENU_PATH.DRIVE },
    ],
  },
  {
    code: 'SYSTEM',
    name: '시스템 설정',
    children: [
      { code: MENU_CODE.ROLES, name: '권한 관리', to: MENU_PATH.ROLES },
      { code: MENU_CODE.CODE_RULES, name: '코드 채번 규칙', to: MENU_PATH.CODE_RULES },
    ],
  },
];
