import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import {
  DERIVED_CACHE_TAGS,
  PROFILE_CACHE_TAG,
} from '@/shared/api/cacheDependencies';
import { todayStamp } from '@/shared/api/excelDownload';
import { useBlobDownload } from '@/shared/api/useBlobDownload';
import type { PageResponse } from '@/shared/types/api';
import type {
  EmployeeCreateRequest,
  EmployeeDetail,
  EmployeeProfileResponse,
  EmployeeReference,
  EmployeeReferenceSearchParams,
  EmployeeSearchParams,
  EmployeeSummary,
  EmployeeUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

export const employeeApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMyProfile: builder.query<EmployeeProfileResponse, void>({
      query: () => ({ url: '/api/v1/employees/me', method: 'GET' }),
      providesTags: [PROFILE_CACHE_TAG],
    }),
    getEmployees: builder.query<PageResponse<EmployeeSummary>, EmployeeSearchParams>({
      query: (params) => ({
        url: '/api/v1/employees',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Employee', id: 'LIST' },
        ...(result?.content.map((m) => ({ type: 'Employee' as const, id: m.id })) ?? []),
      ],
    }),
    getEmployeeReferences: builder.query<
      PageResponse<EmployeeReference>,
      EmployeeReferenceSearchParams
    >({
      query: (params) => ({
        url: '/api/v1/employees/reference',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Employee', id: 'REFERENCE_LIST' },
        ...(result?.content.map((employee) => ({
          type: 'Employee' as const,
          id: employee.id,
        })) ?? []),
      ],
    }),
    getContractEmployeeReferences: builder.query<
      PageResponse<EmployeeReference>,
      EmployeeReferenceSearchParams
    >({
      query: (params) => ({
        url: '/api/v1/employees/contract-reference',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Employee', id: 'CONTRACT_REFERENCE_LIST' },
        ...(result?.content.map((employee) => ({
          type: 'Employee' as const,
          id: employee.id,
        })) ?? []),
      ],
    }),
    getEmployee: builder.query<EmployeeDetail, number>({
      query: (id) => ({ url: `/api/v1/employees/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Employee', id }],
    }),
    createEmployee: builder.mutation<number, EmployeeCreateRequest>({
      query: (body) => ({ url: '/api/v1/employees', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Employee', id: 'LIST' },
        { type: 'Employee', id: 'REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.employee,
      ],
    }),
    updateEmployee: builder.mutation<void, { id: number; body: EmployeeUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/employees/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Employee', id },
        { type: 'Employee', id: 'LIST' },
        { type: 'Employee', id: 'REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.employee,
      ],
    }),
    deleteEmployee: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/employees/${id}`, method: 'DELETE' }),
      invalidatesTags: [
        { type: 'Employee', id: 'LIST' },
        { type: 'Employee', id: 'REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.employee,
      ],
    }),
    deleteEmployees: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/employees', method: 'DELETE', data: ids }),
      invalidatesTags: [
        { type: 'Employee', id: 'LIST' },
        { type: 'Employee', id: 'REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.employee,
      ],
    }),
    checkLoginIdAvailability: builder.query<{ available: boolean }, string>({
      query: (loginId) => ({
        url: '/api/v1/employees/availability',
        method: 'GET',
        params: { loginId },
      }),
    }),
  }),
});

export const {
  useGetMyProfileQuery,
  useGetEmployeesQuery,
  useGetEmployeeReferencesQuery,
  useGetContractEmployeeReferencesQuery,
  useGetEmployeeQuery,
  useCreateEmployeeMutation,
  useUpdateEmployeeMutation,
  useDeleteEmployeeMutation,
  useDeleteEmployeesMutation,
  useCheckLoginIdAvailabilityQuery,
} = employeeApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 * 토큰 주입 + 브라우저 다운로드 트리거까지 처리.
 */
export function useDownloadEmployeesExcel() {
  const download = useBlobDownload();

  return useCallback(
    (params: Omit<EmployeeSearchParams, 'page' | 'size'>) => download({
      url: '/api/v1/employees/excel',
      fallbackName: `employees_${todayStamp()}.xlsx`,
      params: cleanParams(params),
    }),
    [download],
  );
}
