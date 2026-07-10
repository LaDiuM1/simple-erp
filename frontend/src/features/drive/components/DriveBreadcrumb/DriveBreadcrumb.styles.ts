import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

/** 테이블 상단의 breadcrumb 영역 — 아래 테이블과 구분선으로 분리 (FilterBarArea 와 동일 톤). */
export const BreadcrumbBar = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  flexWrap: 'wrap',
  gap: '0.375rem',
  padding: '0.875rem 1.25rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  flexShrink: 0,
}));

/** 상위 경로 crumb — 클릭 시 해당 폴더로 이동. */
export const CrumbButton = styled('button')(({ theme }) => ({
  background: 'none',
  border: 'none',
  padding: 0,
  margin: 0,
  cursor: 'pointer',
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  '&:hover': {
    color: theme.palette.primary.main,
    textDecoration: 'underline',
  },
}));

/** 현재 폴더 crumb — 클릭 불가, 강조 텍스트. */
export const CurrentCrumb = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));

export const CrumbSeparator = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.disabled,
}));
