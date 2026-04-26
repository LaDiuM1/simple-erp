import { useTheme } from '@mui/material/styles';
import StatusDot from '@/shared/ui/atoms/StatusDot';
import { DEPARTURE_TYPE_LABELS, type DepartureType } from '@/features/salesContact/types';

interface Props {
  active: boolean;
  endDate: string | null;
  departureType: DepartureType | null;
}

export default function EmploymentStatusIndicator({ active, endDate, departureType }: Props) {
  const theme = useTheme();
  if (active) {
    return <StatusDot color={theme.palette.statusActive} label="재직 중" />;
  }
  const label = departureType
    ? `${DEPARTURE_TYPE_LABELS[departureType]}${endDate ? ` · ${endDate}` : ''}`
    : (endDate ? `${endDate} 종료` : '종료');
  return <StatusDot color={theme.palette.text.disabled} label={label} />;
}
