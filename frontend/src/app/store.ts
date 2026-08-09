import { configureStore } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import authReducer from '@/features/auth/store/authSlice';
import demoRuntimeReducer from '@/shared/demo/demoRuntimeSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { api } from '@/shared/api/baseApi';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    demoRuntime: demoRuntimeReducer,
    snackbar: snackbarReducer,
    [api.reducerPath]: api.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(api.middleware),
});

setupListeners(store.dispatch);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
