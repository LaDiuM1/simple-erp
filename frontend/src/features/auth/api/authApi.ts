import { api } from '@/shared/api/baseApi';
import type { LoginRequest, TokenResponse } from '../types';

const authApi = api.injectEndpoints({
  endpoints: (builder) => ({
    login: builder.mutation<TokenResponse, LoginRequest>({
      query: (credentials) => ({
        url: '/api/v1/auth/login',
        method: 'POST',
        data: credentials,
      }),
    }),
  }),
});

export const { useLoginMutation } = authApi;
