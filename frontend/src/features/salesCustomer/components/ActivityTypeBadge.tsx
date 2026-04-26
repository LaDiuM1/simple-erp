import Chip from '@mui/material/Chip';
import {
  SALES_ACTIVITY_TYPE_LABELS,
  type SalesActivityType,
} from '@/features/salesCustomer/types';

const COLOR_MAP: Record<SalesActivityType, 'primary' | 'success' | 'info' | 'warning' | 'default'> = {
  VISIT: 'primary',
  CALL: 'success',
  MEETING: 'info',
  EMAIL: 'warning',
  OTHER: 'default',
};

export default function ActivityTypeBadge({ type }: { type: SalesActivityType }) {
  return (
    <Chip
      size="small"
      label={SALES_ACTIVITY_TYPE_LABELS[type]}
      color={COLOR_MAP[type]}
      variant="outlined"
      sx={{ fontWeight: 600, fontSize: '0.75rem', height: 22 }}
    />
  );
}
