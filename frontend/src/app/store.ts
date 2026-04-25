import { configureStore } from '@reduxjs/toolkit';
import authReducer from '@/features/auth/store/authSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { api } from '@/shared/api/baseApi';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    snackbar: snackbarReducer,
    [api.reducerPath]: api.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(api.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
