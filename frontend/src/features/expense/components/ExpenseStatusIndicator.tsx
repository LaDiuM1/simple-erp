import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { EXPENSE_STATUS_LABELS, type ExpenseStatus } from '@/features/expense/types';

/**
 * ExpenseStatus 전용 텍스트 표시.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function ExpenseStatusIndicator({ status }: { status: ExpenseStatus }) {
  const theme = useTheme();
  const colorMap: Record<ExpenseStatus, string> = {
    IN_PROGRESS: theme.palette.statusPending,
    APPROVED: theme.palette.statusActive,
    REJECTED: theme.palette.error.main,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {EXPENSE_STATUS_LABELS[status]}
    </Typography>
  );
}
