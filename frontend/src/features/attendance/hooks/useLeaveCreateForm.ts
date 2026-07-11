import * as React from 'react';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useCreateLeaveMutation } from '@/features/attendance/api/attendanceApi';
import { LEAVES_PATH } from '@/features/attendance/config/attendancePaths';
import {
  emptyLeaveForm,
  leaveFormToCreateRequest,
  type LeaveBalance,
  type LeaveFormValues,
  type LeaveType,
} from '@/features/attendance/types';
import {
  calculateDeductedDays,
  isDeductibleLeaveType,
  isHalfDayLeaveType,
} from '@/features/attendance/utils/leaveDays';

export const LEAVE_CREATE_FORM_ID = 'leave-create-form';

export interface LeaveCreateFormState {
  values: LeaveFormValues;
  update: <K extends keyof LeaveFormValues>(key: K, value: LeaveFormValues[K]) => void;
  isHalfDay: boolean;
  isDeductible: boolean;
  /** 예상 차감 일수 — BE LeaveService 계산 규칙 미러 (실시간 표시용). */
  deductedDays: number;
  /** 현재 연도 잔여 연차 — 조회 전이면 undefined (경고 미표시). */
  remainingDays: number | undefined;
  insufficientBalance: boolean;
  isSaving: boolean;
  confirmOpen: boolean;
  handleTypeChange: (type: LeaveType) => void;
  handleStartDateChange: (date: string) => void;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
}

/**
 * 휴가 신청 form-state hook — 유형 / 기간 동기화 (반차 = 시작일 하루 고정) + 예상 차감 일수 실시간 계산.
 * 신청 = 즉시 상신이라 submit 전 ConfirmModal 로 상신 의사 확인 (경비 청구와 동형).
 * balance 는 page hook 이 조회해 주입 — 잔여 부족 선제 차단은 시작일 연도와 잔여 조회 연도가
 * 일치할 때만 적용 (연도가 다르면 BE 가 시작일 연도 잔여로 최종 검증).
 */
export function useLeaveCreateForm(balance: LeaveBalance | undefined): LeaveCreateFormState {
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const { values, updateField: update, setAll } = useFormState<LeaveFormValues>(() => emptyLeaveForm());
  const [confirmOpen, confirm] = useToggle();
  const [createLeave, { isLoading: isSaving }] = useCreateLeaveMutation();

  const isHalfDay = isHalfDayLeaveType(values.leaveType);
  const isDeductible = isDeductibleLeaveType(values.leaveType);
  const deductedDays = calculateDeductedDays(values.leaveType, values.startDate, values.endDate);
  // 시작일 연도 ≠ 잔여 조회 연도면 차단하지 않는다 (예: 연말에 내년 연차 신청).
  const insufficientBalance =
    isDeductible &&
    balance !== undefined &&
    Number(values.startDate.slice(0, 4)) === balance.year &&
    deductedDays > balance.remainingDays;

  const handleTypeChange = (type: LeaveType) => {
    setAll((prev) =>
      isHalfDayLeaveType(type)
        ? { ...prev, leaveType: type, endDate: prev.startDate }
        : { ...prev, leaveType: type },
    );
  };

  const handleStartDateChange = (date: string) => {
    setAll((prev) =>
      isHalfDayLeaveType(prev.leaveType)
        ? { ...prev, startDate: date, endDate: date }
        : { ...prev, startDate: date },
    );
  };

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.startDate === '' || values.endDate === '') {
      snackbar.error('휴가 기간을 선택해주세요.');
      return;
    }
    if (values.startDate > values.endDate) {
      snackbar.error('종료일은 시작일보다 빠를 수 없습니다.');
      return;
    }
    if (values.approvalLine.length === 0) {
      snackbar.error('결재자를 1명 이상 추가해주세요.');
      return;
    }
    if (insufficientBalance) {
      snackbar.error('잔여 연차가 부족합니다.');
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(createLeave(leaveFormToCreateRequest(values)), {
      success: '휴가 신청이 상신되었습니다.',
      navigateTo: LEAVES_PATH,
    });
  };

  return {
    values,
    update,
    isHalfDay,
    isDeductible,
    deductedDays,
    remainingDays: balance?.remainingDays,
    insufficientBalance,
    isSaving,
    confirmOpen,
    handleTypeChange,
    handleStartDateChange,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
  };
}
