import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import Muted from '@/shared/ui/atoms/Muted';
import { warrantyStatusOf } from '@/features/equipment/types';

/**
 * 보증 만료일 텍스트 — 만료 / 90일 내 임박 / 유효를 텍스트 색으로만 차등 (chip/badge 미사용).
 * 만료일 미입력 (보증 정보 미보완) 은 muted dash.
 */
export default function WarrantyDateText({ endDate }: { endDate: string | null }) {
  const theme = useTheme();
  const status = warrantyStatusOf(endDate);
  if (!status) return <Muted />;

  const color = {
    active: theme.palette.text.primary,
    expiring: theme.palette.warning.main,
    expired: theme.palette.text.disabled,
  }[status];

  return (
    <Typography component="span" sx={{ fontSize: 'inherit', color, fontWeight: status === 'expiring' ? 500 : undefined }}>
      {endDate}
      {status === 'expired' ? ' (만료)' : ''}
    </Typography>
  );
}
