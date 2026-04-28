import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { EMPLOYEE_STATUS_LABELS, type EmployeeStatus } from '@/features/employee/types';

/**
 * EmployeeStatus 전용 텍스트 표시.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function EmployeeStatusIndicator({ status }: { status: EmployeeStatus }) {
  const theme = useTheme();
  const colorMap: Record<EmployeeStatus, string> = {
    ACTIVE: theme.palette.statusActive,
    LEAVE: theme.palette.statusPending,
    RESIGNED: theme.palette.text.disabled,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {EMPLOYEE_STATUS_LABELS[status]}
    </Typography>
  );
}
