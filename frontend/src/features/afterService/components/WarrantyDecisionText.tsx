import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import { WARRANTY_DECISION_LABELS, type WarrantyDecision } from '@/features/afterService/types';

/**
 * 유상 / 무상 판정 텍스트 표시 — 시각 차등은 텍스트 색만 (chip/badge 미사용).
 */
export default function WarrantyDecisionText({ decision }: { decision: WarrantyDecision }) {
  const theme = useTheme();
  const colorMap: Record<WarrantyDecision, string> = {
    UNDECIDED: theme.palette.text.secondary,
    FREE: theme.palette.statusActive,
    PAID: theme.palette.warning.main,
  };
  return (
    <Typography
      component="span"
      sx={{ fontSize: 'inherit', color: colorMap[decision], fontWeight: 500 }}
    >
      {WARRANTY_DECISION_LABELS[decision]}
    </Typography>
  );
}
