import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { LEAVE_STATUS_LABELS, type LeaveStatus } from '@/features/attendance/types';

/**
 * LeaveStatus 전용 텍스트 표시 (ExpenseStatusIndicator 와 동형).
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function LeaveStatusIndicator({ status }: { status: LeaveStatus }) {
  const theme = useTheme();
  const colorMap: Record<LeaveStatus, string> = {
    IN_PROGRESS: theme.palette.statusPending,
    APPROVED: theme.palette.statusActive,
    REJECTED: theme.palette.error.main,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {LEAVE_STATUS_LABELS[status]}
    </Typography>
  );
}
