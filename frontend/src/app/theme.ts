import { createTheme, type Shadows } from '@mui/material/styles';

const shadows: Shadows = [
  'none',
  '0 1px 2px 0 rgb(0 0 0 / 0.05)',
  '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
  '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
  '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
  '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
  ...Array<string>(19).fill('0 25px 50px -12px rgb(0 0 0 / 0.25)'),
] as Shadows;

declare module '@mui/material/styles' {
  interface Palette {
    primarySubtle: string;
    primaryLight: string;
    errorBg: string;
    errorBorder: string;
  }
  interface PaletteOptions {
    primarySubtle?: string;
    primaryLight?: string;
    errorBg?: string;
    errorBorder?: string;
  }
}

const theme = createTheme({
  breakpoints: {
    values: { xs: 0, sm: 480, md: 768, lg: 1024, xl: 1280 },
  },
  palette: {
    primary: {
      main: '#3B82F6',
      dark: '#2563EB',
      light: '#93C5FD',
      contrastText: '#ffffff',
    },
    primarySubtle: '#EFF6FF',
    primaryLight: '#DBEAFE',
    background: {
      default: '#F8FAFC',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#1E293B',
      secondary: '#64748B',
      disabled: '#94A3B8',
    },
    divider: '#E2E8F0',
    error: {
      main: '#EF4444',
      dark: '#dc2626',
      light: '#FEF2F2',
      contrastText: '#ffffff',
    },
    errorBg: '#FEF2F2',
    errorBorder: '#FECACA',
    success: {
      main: '#10B981',
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
  },
  shadows,
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        'html, body, #root': { height: '100%' },
        body: {
          backgroundColor: '#F8FAFC',
          color: '#1E293B',
          lineHeight: 1.5,
          WebkitFontSmoothing: 'antialiased',
        },
        a: { color: 'inherit', textDecoration: 'none' },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: { textTransform: 'none', borderRadius: '6px' },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: { borderRadius: '6px' },
      },
    },
  },
});

export default theme;
