import StatusDot from '@/shared/ui/atoms/StatusDot';
import { EMPLOYEE_STATUS_LABELS, type EmployeeStatus } from '@/features/employee/types';

const EMPLOYEE_STATUS_COLOR: Record<EmployeeStatus, string> = {
  ACTIVE: '#10B981',
  LEAVE: '#F59E0B',
  RESIGNED: '#94A3B8',
};

/**
 * EmployeeStatus 전용 래퍼.
 * 상태값을 받아 색/라벨 매핑과 함께 공용 StatusDot 에 전달한다.
 */
export default function EmployeeStatusIndicator({ status }: { status: EmployeeStatus }) {
  return <StatusDot color={EMPLOYEE_STATUS_COLOR[status]} label={EMPLOYEE_STATUS_LABELS[status]} />;
}
