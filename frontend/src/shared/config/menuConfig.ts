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
    children: [{ code: 'MDM_HRM', name: '직원 관리', to: '/members' }],
  },
];
