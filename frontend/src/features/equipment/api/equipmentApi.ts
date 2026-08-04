import { useCallback } from 'react';
import { api } from '@/shared/api/baseApi';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, todayStamp, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';
import {
  DASHBOARD_CACHE_TAGS,
  DERIVED_CACHE_TAGS,
} from '@/shared/api/cacheDependencies';
import type { PageResponse } from '@/shared/types/api';
import type {
  EquipmentCreateRequest,
  EquipmentDetail,
  EquipmentReference,
  EquipmentReferenceSearchParams,
  EquipmentSearchParams,
  EquipmentSummary,
  EquipmentUpdateRequest,
} from '../types';

function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== ''),
  ) as Partial<T>;
}

export const equipmentApi = api.injectEndpoints({
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
    getEquipmentReferences: builder.query<
      PageResponse<EquipmentReference>,
      EquipmentReferenceSearchParams
    >({
      query: (params) => ({
        url: '/api/v1/equipments/reference',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: (result) => [
        { type: 'Equipment', id: 'REFERENCE_LIST' },
        ...(result?.content.map((equipment) => ({
          type: 'Equipment' as const,
          id: equipment.id,
        })) ?? []),
      ],
    }),
    getEquipmentReference: builder.query<
      EquipmentReference,
      { id: number; customerId: number }
    >({
      query: ({ id, customerId }) => ({
        url: `/api/v1/equipments/reference/${id}`,
        method: 'GET',
        params: { customerId },
      }),
      providesTags: (_result, _error, { id }) => [{ type: 'Equipment', id }],
    }),
    getEquipment: builder.query<EquipmentDetail, number>({
      query: (id) => ({ url: `/api/v1/equipments/${id}`, method: 'GET' }),
      providesTags: (_result, _error, id) => [{ type: 'Equipment', id }],
    }),
    createEquipment: builder.mutation<number, EquipmentCreateRequest>({
      query: (body) => ({ url: '/api/v1/equipments', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Equipment', id: 'LIST' },
        { type: 'Equipment', id: 'REFERENCE_LIST' },
        DASHBOARD_CACHE_TAGS.warranty,
      ],
    }),
    updateEquipment: builder.mutation<void, { id: number; body: EquipmentUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/api/v1/equipments/${id}`, method: 'PUT', data: body }),
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Equipment', id },
        { type: 'Equipment', id: 'LIST' },
        { type: 'Equipment', id: 'REFERENCE_LIST' },
        ...DERIVED_CACHE_TAGS.equipment,
        DASHBOARD_CACHE_TAGS.warranty,
      ],
    }),
    deleteEquipment: builder.mutation<void, number>({
      query: (id) => ({ url: `/api/v1/equipments/${id}`, method: 'DELETE' }),
      invalidatesTags: [
        { type: 'Equipment', id: 'LIST' },
        { type: 'Equipment', id: 'REFERENCE_LIST' },
        DASHBOARD_CACHE_TAGS.warranty,
      ],
    }),
    deleteEquipments: builder.mutation<void, number[]>({
      query: (ids) => ({ url: '/api/v1/equipments', method: 'DELETE', data: ids }),
      invalidatesTags: [
        { type: 'Equipment', id: 'LIST' },
        { type: 'Equipment', id: 'REFERENCE_LIST' },
        DASHBOARD_CACHE_TAGS.warranty,
      ],
    }),
  }),
});

export const {
  useGetEquipmentsQuery,
  useGetEquipmentReferencesQuery,
  useGetEquipmentReferenceQuery,
  useGetEquipmentQuery,
  useCreateEquipmentMutation,
  useUpdateEquipmentMutation,
  useDeleteEquipmentMutation,
  useDeleteEquipmentsMutation,
} = equipmentApi;

/**
 * ?��? ?�일?� binary ?�답?�라 RTK Query baseQuery(JSON ?�싱)?� 맞�? ?�아 axios�?직접 ?�출.
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
