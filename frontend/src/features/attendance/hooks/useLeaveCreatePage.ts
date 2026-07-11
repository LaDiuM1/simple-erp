import { useNavigate } from 'react-router-dom';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetMyLeaveBalanceQuery } from '@/features/attendance/api/attendanceApi';
import { LEAVES_PATH } from '@/features/attendance/config/attendancePaths';
import {
  LEAVE_CREATE_FORM_ID,
  useLeaveCreateForm,
} from '@/features/attendance/hooks/useLeaveCreateForm';

/**
 * 휴가 신청 page hook — 잔여 연차 조회 + form-state hook + headerActions 묶음.
 * 잔여 연차는 현재 연도 기준 (BE 기본값) — 최종 잔여 검증은 BE 가 시작일 연도로 수행.
 */
export function useLeaveCreatePage() {
  const navigate = useNavigate();
  const balanceQuery = useGetMyLeaveBalanceQuery();
  const form = useLeaveCreateForm(balanceQuery.data);

  const headerActions: PageHeaderAction[] = [
    { design: 'cancel', onClick: () => navigate(LEAVES_PATH), disabled: form.isSaving },
    { design: 'create', label: '신청', formId: LEAVE_CREATE_FORM_ID, loading: form.isSaving },
  ];

  return { form, headerActions };
}
