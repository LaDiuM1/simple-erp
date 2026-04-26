import { createElement } from 'react';
import { alpha, createTheme, type Shadows } from '@mui/material/styles';
import {
  ThinCheckedIcon,
  ThinIndeterminateIcon,
  ThinUncheckedIcon,
} from './checkboxIcons';

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
    /** 헤더 / 사이드바 / 테이블 헤더 셀의 옅은 회색 배경 (#F8FAFC) */
    headerBg: string;
    /** 성공/완료 chip 의 배경 (#D1FAE5) */
    successBg: string;
    /** 성공/완료 chip 의 강조 텍스트 (#065F46) */
    successDark: string;
    /** 직원 상태 등 status indicator 의 활성 색 (#10B981) */
    statusActive: string;
    /** 직원 상태 등 status indicator 의 대기/휴직 색 (#F59E0B) */
    statusPending: string;
    /** 로그인 페이지 background gradient (CSS background 값 통째) */
    loginGradient: string;
    /** 직원 프로필 카드 헤더 gradient (CSS background 값 통째) */
    profileGradient: string;
  }
  interface PaletteOptions {
    primarySubtle?: string;
    primaryLight?: string;
    errorBg?: string;
    errorBorder?: string;
    headerBg?: string;
    successBg?: string;
    successDark?: string;
    statusActive?: string;
    statusPending?: string;
    loginGradient?: string;
    profileGradient?: string;
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
      default: '#FFFFFF',
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
      main: '#0f903a',
    },
    headerBg: '#F8FAFC',
    successBg: '#D1FAE5',
    successDark: '#065F46',
    statusActive: '#10B981',
    statusPending: '#F59E0B',
    loginGradient: 'linear-gradient(135deg, #eff6ff 0%, #f0f9ff 50%, #f0fdf4 100%)',
    profileGradient: 'linear-gradient(135deg, #EFF6FF 0%, #F0F9FF 100%)',
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
      styleOverrides: ({ palette }) => ({
        'html, body, #root': { height: '100%' },
        body: {
          backgroundColor: palette.background.paper,
          color: palette.text.primary,
          lineHeight: 1.5,
          WebkitFontSmoothing: 'antialiased',
        },
        a: { color: 'inherit', textDecoration: 'none' },
      }),
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: { textTransform: 'none', borderRadius: '6px' },
      },
    },
    /**
     * 폼 입력창의 기본 variant — filled. (필터바의 SearchTextField 는 사용처에서 explicit
     * variant="outlined" 로 override 하여 컴팩트한 검색 톤 유지)
     */
    MuiTextField: {
      defaultProps: { variant: 'filled' },
    },
    /**
     * Filled 변형의 모던 스타일 (직사각형 / white bg / 옅은 보더).
     * - bg: 항상 white (lift 없음 — 처음부터 깔끔한 흰색)
     * - 보더: inset box-shadow 로 1px 라인 (filled 에는 notchedOutline 이 없음)
     *   - idle: divider (매우 옅음)
     *   - hover: text.disabled (한 단계 darken)
     *   - focus: primary 1px + outer glow ring
     * - radius: 0 — rectangular
     * - underline: 제거 (defaultProps)
     */
    MuiFilledInput: {
      defaultProps: { disableUnderline: true },
      styleOverrides: {
        root: ({ theme: t }) => ({
          borderRadius: 0,
          backgroundColor: t.palette.background.paper,
          transition: 'box-shadow 0.15s ease',
          boxShadow: `inset 0 0 0 1px ${t.palette.divider}`,
          '&:hover:not(.Mui-disabled):not(.Mui-error):not(.Mui-focused)': {
            boxShadow: `inset 0 0 0 1px ${t.palette.text.disabled}`,
          },
          '&.Mui-focused': {
            boxShadow: `inset 0 0 0 1px ${t.palette.primary.main}, 0 0 0 3px ${alpha(t.palette.primary.main, 0.12)}`,
          },
          '&.Mui-error': {
            boxShadow: `inset 0 0 0 1px ${t.palette.error.main}`,
          },
          '&.Mui-error.Mui-focused': {
            boxShadow: `inset 0 0 0 1px ${t.palette.error.main}, 0 0 0 3px ${alpha(t.palette.error.main, 0.12)}`,
          },
          '&.Mui-disabled': {
            backgroundColor: alpha(t.palette.text.primary, 0.02),
          },
        }),
      },
    },
    /**
     * Outlined 입력창 톤 (필터바에서만 사용).
     * - idle: 매우 옅은 divider 라인 — 백그라운드와 자연스럽게 어울림
     * - hover: text.disabled 로 한 단계 darkening (subtle)
     * - focus: primary blue 1px 라인 (layout shift 방지) + 옅은 outer glow ring
     * - 전환은 smooth — modern admin UI 느낌
     */
    MuiOutlinedInput: {
      styleOverrides: {
        root: ({ theme: t }) => ({
          borderRadius: '6px',
          transition: 'box-shadow 0.15s ease',
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: t.palette.divider,
            transition: 'border-color 0.15s ease',
          },
          '&:hover:not(.Mui-disabled):not(.Mui-error) .MuiOutlinedInput-notchedOutline': {
            borderColor: t.palette.text.disabled,
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: t.palette.primary.main,
            borderWidth: '1px',
          },
          '&.Mui-focused:not(.Mui-error)': {
            boxShadow: `0 0 0 3px ${alpha(t.palette.primary.main, 0.12)}`,
          },
          '&.Mui-error.Mui-focused': {
            boxShadow: `0 0 0 3px ${alpha(t.palette.error.main, 0.12)}`,
          },
          '&.Mui-disabled': {
            backgroundColor: alpha(t.palette.text.primary, 0.02),
          },
        }),
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: ({ theme: t }) => ({
          color: t.palette.text.secondary,
          fontWeight: 500,
          '&.Mui-focused': {
            color: t.palette.primary.main,
          },
        }),
      },
    },
    MuiFormHelperText: {
      styleOverrides: {
        root: {
          fontSize: '0.75rem',
          marginTop: '0.375rem',
          /** filled input 의 내부 padding-left (12px) 와 세로 기준선 정렬 */
          marginLeft: '12px',
        },
      },
    },
    MuiFormLabel: {
      styleOverrides: {
        asterisk: ({ theme: t }) => ({
          color: t.palette.error.main,
          fontWeight: 700,
          marginLeft: '0.125rem',
        }),
      },
    },
    /**
     * 체크박스 아이콘을 1px border + 옅은 톤의 커스텀 Box 로 교체.
     * MUI 기본 SVG 는 2px stroke 라 시각적으로 무거움.
     */
    MuiCheckbox: {
      defaultProps: {
        icon: createElement(ThinUncheckedIcon),
        checkedIcon: createElement(ThinCheckedIcon),
        indeterminateIcon: createElement(ThinIndeterminateIcon),
      },
    },
  },
});

export default theme;
