import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';

/**
 * 근태 도메인 라우트 상수 — 메뉴 경로 (/attendance) 밖의 휴가 서브 경로가 여러 hook 에
 * 흩어져 있어 한 곳으로 모음. app/routes.tsx 의 라우트 정의와 동기 유지.
 */
export const ATTENDANCE_PATH = MENU_PATH[MENU_CODE.ATTENDANCE];
export const ATTENDANCE_STATUS_PATH = `${ATTENDANCE_PATH}/status`;

export const LEAVES_PATH = '/leaves';
export const LEAVE_CREATE_PATH = `${LEAVES_PATH}/new`;
export const LEAVE_STATUS_PATH = `${LEAVES_PATH}/status`;
export const LEAVE_BALANCES_PATH = `${LEAVES_PATH}/balances`;
