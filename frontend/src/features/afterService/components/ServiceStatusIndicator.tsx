import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { SERVICE_STATUS_LABELS, type ServiceStatus } from '@/features/afterService/types';

/**
 * ServiceStatus 전용 텍스트 표시.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function ServiceStatusIndicator({ status }: { status: ServiceStatus }) {
  const theme = useTheme();
  const colorMap: Record<ServiceStatus, string> = {
    RECEIVED: theme.palette.text.primary,
    ASSIGNED: theme.palette.info.main,
    IN_PROGRESS: theme.palette.statusPending,
    COMPLETED: theme.palette.statusActive,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {SERVICE_STATUS_LABELS[status]}
    </Typography>
  );
}
