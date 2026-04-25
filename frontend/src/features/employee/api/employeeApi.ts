import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { useAppSelector } from '@/app/hooks';
import type { PageResponse } from '@/shared/types/api';
import type {
  EmployeeCreateRequest,
  EmployeeDetail,
  EmployeeProfileResponse,
  EmployeeSearchParams,
  EmployeeSummary,
  EmployeeUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const employeeApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMyProfile: builder.query<EmployeeProfileResponse, void>({
      query: () => ({ url: '/api/v1/employees/me', method: 'GET' }),
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
    getEmployee: builder.query<EmployeeDetail, number>({
      query: (id) => ({ url: `/api/v1/employees/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Employee', id }],
    }),
    createEmployee: builder.mutation<number, EmployeeCreateRequest>({
      query: (body) => ({ url: '/api/v1/employees', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Employee', id: 'LIST' }],
    }),
    updateEmployee: builder.mutation<void, { id: number; body: EmployeeUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/employees/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Employee', id },
        { type: 'Employee', id: 'LIST' },
      ],
    }),
    deleteEmployee: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/employees/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Employee', id: 'LIST' }],
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
  useGetEmployeeQuery,
  useCreateEmployeeMutation,
  useUpdateEmployeeMutation,
  useDeleteEmployeeMutation,
  useCheckLoginIdAvailabilityQuery,
} = employeeApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 * 토큰 주입 + 브라우저 다운로드 트리거까지 처리.
 */
export function useDownloadEmployeesExcel() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (params: Omit<EmployeeSearchParams, 'page' | 'size'>) => {
      const response = await axiosInstance.get('/api/v1/employees/excel', {
        params: cleanParams(params),
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });

      const filename = extractFilename(response.headers['content-disposition'])
        ?? `employees_${todayStamp()}.xlsx`;

      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}

function triggerBrowserDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function extractFilename(contentDisposition: string | undefined): string | null {
  if (!contentDisposition) return null;
  const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
  if (!match) return null;
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
}

function todayStamp(): string {
  const d = new Date();
  return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
}
