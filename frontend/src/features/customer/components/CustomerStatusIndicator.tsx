import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { CUSTOMER_STATUS_LABELS, type CustomerStatus } from '@/features/customer/types';

/**
 * CustomerStatus 전용 텍스트 표시.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function CustomerStatusIndicator({ status }: { status: CustomerStatus }) {
  const theme = useTheme();
  const colorMap: Record<CustomerStatus, string> = {
    ACTIVE: theme.palette.statusActive,
    INACTIVE: theme.palette.text.disabled,
    SUSPENDED: theme.palette.statusPending,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {CUSTOMER_STATUS_LABELS[status]}
    </Typography>
  );
}
