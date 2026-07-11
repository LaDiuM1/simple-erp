import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { APPROVAL_STATUS_LABELS, type ApprovalStatus } from '@/features/approval/types';

/**
 * ApprovalStatus 전용 텍스트 표시.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function ApprovalStatusIndicator({ status }: { status: ApprovalStatus }) {
  const theme = useTheme();
  const colorMap: Record<ApprovalStatus, string> = {
    IN_PROGRESS: theme.palette.primary.main,
    APPROVED: theme.palette.statusActive,
    REJECTED: theme.palette.error.main,
    CANCELED: theme.palette.text.disabled,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {APPROVAL_STATUS_LABELS[status]}
    </Typography>
  );
}
