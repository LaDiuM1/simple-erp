import { api } from '@/shared/api/baseApi';
import type { MemberProfileResponse } from '../types';

const memberApi = api.injectEndpoints({
  endpoints: (builder) => ({
    getMyProfile: builder.query<MemberProfileResponse, void>({
      query: () => ({
        url: '/api/v1/members/me',
        method: 'GET',
      }),
    }),
  }),
});

export const { useGetMyProfileQuery } = memberApi;
