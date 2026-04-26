import { useTheme } from '@mui/material/styles';
import StatusDot from '@/shared/ui/atoms/StatusDot';
import { CUSTOMER_STATUS_LABELS, type CustomerStatus } from '@/features/customer/types';

/**
 * CustomerStatus 전용 래퍼.
 */
export default function CustomerStatusIndicator({ status }: { status: CustomerStatus }) {
  const theme = useTheme();
  const colorMap: Record<CustomerStatus, string> = {
    ACTIVE: theme.palette.statusActive,
    INACTIVE: theme.palette.text.disabled,
    SUSPENDED: theme.palette.statusPending,
  };
  return <StatusDot color={colorMap[status]} label={CUSTOMER_STATUS_LABELS[status]} />;
}
