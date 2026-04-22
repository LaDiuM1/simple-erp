import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

const TOKEN_KEY = 'erp-access-token';

interface AuthState {
  accessToken: string | null;
}

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    accessToken: localStorage.getItem(TOKEN_KEY),
  } as AuthState,
  reducers: {
    setToken: (state, action: PayloadAction<string>) => {
      state.accessToken = action.payload;
      localStorage.setItem(TOKEN_KEY, action.payload);
    },
    logout: (state) => {
      state.accessToken = null;
      localStorage.removeItem(TOKEN_KEY);
    },
  },
});

export const { setToken, logout } = authSlice.actions;
export default authSlice.reducer;
