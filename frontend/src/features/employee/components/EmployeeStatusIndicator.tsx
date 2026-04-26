import { useTheme } from '@mui/material/styles';
import StatusDot from '@/shared/ui/atoms/StatusDot';
import { EMPLOYEE_STATUS_LABELS, type EmployeeStatus } from '@/features/employee/types';

/**
 * EmployeeStatus 전용 래퍼.
 * 상태값을 받아 색/라벨 매핑과 함께 공용 StatusDot 에 전달한다.
 */
export default function EmployeeStatusIndicator({ status }: { status: EmployeeStatus }) {
  const theme = useTheme();
  const colorMap: Record<EmployeeStatus, string> = {
    ACTIVE: theme.palette.statusActive,
    LEAVE: theme.palette.statusPending,
    RESIGNED: theme.palette.text.disabled,
  };
  return <StatusDot color={colorMap[status]} label={EMPLOYEE_STATUS_LABELS[status]} />;
}
