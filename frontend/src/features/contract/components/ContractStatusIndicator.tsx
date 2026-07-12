import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { CONTRACT_STATUS_LABELS, type ContractStatus } from '@/features/contract/types';

/**
 * ContractStatus 전용 텍스트 표시.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function ContractStatusIndicator({ status }: { status: ContractStatus }) {
  const theme = useTheme();
  const colorMap: Record<ContractStatus, string> = {
    CONTRACTED: theme.palette.text.primary,
    ORDERED: theme.palette.info.main,
    ARRIVED: theme.palette.info.main,
    INSTALLING: theme.palette.statusPending,
    INSTALLED: theme.palette.primary.main,
    SETTLED: theme.palette.statusActive,
    CANCELED: theme.palette.text.disabled,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: '0.875rem', color: colorMap[status], fontWeight: 500 }}
    >
      {CONTRACT_STATUS_LABELS[status]}
    </Typography>
  );
}
