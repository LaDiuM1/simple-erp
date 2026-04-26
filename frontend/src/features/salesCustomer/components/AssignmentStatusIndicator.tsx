import { useTheme } from '@mui/material/styles';
import StatusDot from '@/shared/ui/atoms/StatusDot';

interface Props {
  active: boolean;
  primary: boolean;
  endDate: string | null;
}

export default function AssignmentStatusIndicator({ active, primary, endDate }: Props) {
  const theme = useTheme();
  if (active) {
    return (
      <StatusDot
        color={primary ? theme.palette.primary.main : theme.palette.statusActive}
        label={primary ? '주담당' : '담당'}
      />
    );
  }
  return (
    <StatusDot
      color={theme.palette.text.disabled}
      label={endDate ? `${endDate} 종료` : '종료'}
    />
  );
}
