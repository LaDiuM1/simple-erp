import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

/**
 * 근태 도메인 공용 섹션 surface — 오늘 카드 / 잔여 연차 타일과 같은 카드 톤.
 * 내 출퇴근 (월별 기록) / 내 휴가 (신청 내역) / 잔여 연차 관리가 동일 3종을 사용한다.
 */
export const SurfaceRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  overflow: 'hidden',
}));

export const SurfaceHeaderRow = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.5rem',
  flexWrap: 'wrap',
  padding: '0.75rem 1.25rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const SurfaceTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
}));
