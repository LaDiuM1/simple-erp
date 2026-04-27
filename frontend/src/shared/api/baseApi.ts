import { createApi, type BaseQueryFn } from '@reduxjs/toolkit/query/react';
import { isAxiosError, type Method } from 'axios';
import axiosInstance from './axiosInstance';
import { logout } from '@/features/auth/store/authSlice';
import type { ApiResponse, ApiError } from '@/shared/types/api';
import type { RootState } from '@/app/store';

interface QueryArgs {
  url: string;
  method: Method;
  data?: unknown;
  params?: unknown;
  /** axios paramsSerializer 옵션 — 배열 직렬화 형태 등을 엔드포인트별로 오버라이드. */
  paramsSerializer?: { indexes?: null | boolean };
}

const axiosBaseQuery: BaseQueryFn<QueryArgs, unknown, ApiError> = async (
  { url, method, data, params, paramsSerializer },
  { dispatch, getState },
) => {
  const token = (getState() as RootState).auth.accessToken;

  try {
    const result = await axiosInstance({
      url,
      method,
      data,
      params,
      paramsSerializer,
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    const body = result.data as ApiResponse<unknown>;
    return { data: body.data };
  } catch (error) {
    if (isAxiosError(error) && error.response) {
      const isAuthEndpoint = url.includes('/auth/');

      if (error.response.status === 401 && !isAuthEndpoint) {
        dispatch(logout());
        window.location.href = '/login';
      }

      const body = error.response.data as ApiResponse<null>;
      return {
        error: {
          status: body?.status ?? error.response.status,
          message: body?.message ?? '요청을 처리할 수 없습니다.',
        },
      };
    }

    return {
      error: {
        status: 0,
        message: '서버와 연결할 수 없습니다.',
      },
    };
  }
};

export const api = createApi({
  reducerPath: 'api',
  baseQuery: axiosBaseQuery,
  tagTypes: [
    'Employee',
    'Department',
    'Position',
    'Role',
    'CodeRule',
    'Customer',
    'SalesActivity',
    'SalesAssignment',
    'SalesAggregate',
    'SalesContact',
    'SalesContactEmployment',
    'AcquisitionSource',
  ],
  endpoints: () => ({}),
});
