import type { ReactElement } from 'react';
import { render, type RenderResult } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import theme from '@/app/theme';

/** styled() 컴포넌트가 테마 토큰을 읽을 수 있도록 ThemeProvider 로 감싸 렌더. */
export function renderWithTheme(ui: ReactElement): RenderResult {
  return render(<ThemeProvider theme={theme}>{ui}</ThemeProvider>);
}
