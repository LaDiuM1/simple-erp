import type { AppDispatch } from '@/app/store';
import { api } from '@/shared/api/baseApi';
import { logout, setToken } from './authSlice';

/**
 * 로그인/로그아웃 시 RTK Query 캐시까지 함께 초기화한다.
 * 캐시를 비우지 않으면 직전 사용자의 getMyProfile 등이 새 세션에 노출된다.
 */
export const performLogin = (accessToken: string) => (dispatch: AppDispatch) => {
  dispatch(api.util.resetApiState());
  dispatch(setToken(accessToken));
};

export const performLogout = () => (dispatch: AppDispatch) => {
  dispatch(logout());
  dispatch(api.util.resetApiState());
};
