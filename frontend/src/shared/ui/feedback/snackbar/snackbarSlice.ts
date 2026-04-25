import { createSlice, nanoid, type PayloadAction } from '@reduxjs/toolkit';

export type SnackbarSeverity = 'success' | 'error' | 'info' | 'warning';

export interface SnackbarItem {
  id: string;
  severity: SnackbarSeverity;
  message: string;
  duration?: number;
}

interface SnackbarState {
  queue: SnackbarItem[];
}

const snackbarSlice = createSlice({
  name: 'snackbar',
  initialState: { queue: [] } as SnackbarState,
  reducers: {
    enqueue: {
      reducer: (state, action: PayloadAction<SnackbarItem>) => {
        state.queue.push(action.payload);
      },
      prepare: (payload: Omit<SnackbarItem, 'id'>) => ({
        payload: { id: nanoid(), ...payload },
      }),
    },
    dismiss: (state, action: PayloadAction<string>) => {
      state.queue = state.queue.filter((i) => i.id !== action.payload);
    },
  },
});

export const { enqueue, dismiss } = snackbarSlice.actions;
export default snackbarSlice.reducer;
