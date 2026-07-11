import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, todayStamp, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';
import type { PageResponse } from '@/shared/types/api';
import type {
  EquipmentCreateRequest,
  EquipmentDetail,
  EquipmentSearchParams,
  EquipmentSummary,
  EquipmentUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

const equipmentApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getEquipments: builder.query<PageResponse<EquipmentSummary>, EquipmentSearchParams>({
      query: (params) => ({
        url: '/api/v1/equipments',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Equipment', id: 'LIST' },
        ...(result?.content.map((e) => ({ type: 'Equipment' as const, id: e.id })) ?? []),
      ],
    }),
    getEquipment: builder.query<EquipmentDetail, number>({
      query: (id) => ({ url: `/api/v1/equipments/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Equipment', id }],
    }),
    createEquipment: builder.mutation<number, EquipmentCreateRequest>({
      query: (body) => ({ url: '/api/v1/equipments', method: 'POST', data: body }),
      invalidatesTags: [{ type: 'Equipment', id: 'LIST' }],
    }),
    updateEquipment: builder.mutation<void, { id: number; body: EquipmentUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/equipments/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Equipment', id },
        { type: 'Equipment', id: 'LIST' },
      ],
    }),
    deleteEquipment: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/equipments/${id}`, method: 'DELETE' }),
      invalidatesTags: [{ type: 'Equipment', id: 'LIST' }],
    }),
    deleteEquipments: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/equipments', method: 'DELETE', data: ids }),
      invalidatesTags: [{ type: 'Equipment', id: 'LIST' }],
    }),
  }),
});

export const {
  useGetEquipmentsQuery,
  useGetEquipmentQuery,
  useCreateEquipmentMutation,
  useUpdateEquipmentMutation,
  useDeleteEquipmentMutation,
  useDeleteEquipmentsMutation,
} = equipmentApi;

/**
 * 엑셀 파일은 binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios로 직접 호출.
 */
export function useDownloadEquipmentsExcel() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (params: Omit<EquipmentSearchParams, 'page' | 'size'>) => {
      const response = await axiosInstance.get('/api/v1/equipments/excel', {
        params: cleanParams(params),
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });

      const filename = extractFilename(response.headers['content-disposition'])
        ?? `equipments_${todayStamp()}.xlsx`;

      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}
