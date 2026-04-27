import { api } from '@/shared/api/baseApi';
import type {
  AcquisitionSourceCreateRequest,
  AcquisitionSourceInfo,
} from '../types';

const BASE = '/api/v1/sales-contacts/acquisition-sources';

const acquisitionSourceApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getAcquisitionSources: builder.query<AcquisitionSourceInfo[], void>({
      query: () => ({ url: BASE, method: 'GET' }),
      providesTags: [{ type: 'AcquisitionSource', id: 'ALL' }],
    }),
    createAcquisitionSource: builder.mutation<number, AcquisitionSourceCreateRequest>({
      query: (body) => ({ url: BASE, method: 'POST', data: body }),
      invalidatesTags: [{ type: 'AcquisitionSource', id: 'ALL' }],
    }),
    deleteAcquisitionSource: builder.mutation<void, number>({
      query: (id) => ({ url: `${BASE}/${id}`, method: 'DELETE' }),
      invalidatesTags: [
        { type: 'AcquisitionSource', id: 'ALL' },
        { type: 'SalesContact', id: 'LIST' },
      ],
    }),
  }),
});

export const {
  useGetAcquisitionSourcesQuery,
  useCreateAcquisitionSourceMutation,
  useDeleteAcquisitionSourceMutation,
} = acquisitionSourceApi;
