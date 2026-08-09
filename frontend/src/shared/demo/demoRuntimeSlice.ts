import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

export interface DemoRuntimeState {
  writeBlocked: boolean;
}

const initialState: DemoRuntimeState = {
  // 상태 계약을 해석하기 전에는 쓰기를 허용하지 않는다.
  writeBlocked: true,
};

const demoRuntimeSlice = createSlice({
  name: 'demoRuntime',
  initialState,
  reducers: {
    setDemoWriteBlocked(state, action: PayloadAction<boolean>) {
      state.writeBlocked = action.payload;
    },
  },
});

export const { setDemoWriteBlocked } = demoRuntimeSlice.actions;
export default demoRuntimeSlice.reducer;
