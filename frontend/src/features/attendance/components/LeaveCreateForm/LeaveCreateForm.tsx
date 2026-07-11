import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import CalendarMonthRoundedIcon from '@mui/icons-material/CalendarMonthRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { FormSection } from '@/shared/ui/GenericForm';
import ApprovalLineField from '@/shared/ui/ApprovalLineField';
import { LEAVE_TYPE_OPTIONS, type LeaveType } from '@/features/attendance/types';
import {
  LEAVE_CREATE_FORM_ID,
  type LeaveCreateFormState,
} from '@/features/attendance/hooks/useLeaveCreateForm';
import {
  CreateForm,
  CreateRoot,
  DeductionHint,
  DeductionLabel,
  DeductionRow,
  DeductionSummary,
  DeductionValue,
  DeductionWarning,
  FieldFull,
  FieldGrid,
} from './LeaveCreateForm.styles';

interface Props {
  form: LeaveCreateFormState;
}

/**
 * 휴가 신청 폼 Body — form-state hook 결과를 렌더 (fetch 미관여).
 * 신청 = 즉시 상신이라 submit 전 ConfirmModal 로 확인 (경비 청구와 동형).
 */
export default function LeaveCreateForm({ form }: Props) {
  const { values, update } = form;

  return (
    <>
      <CreateRoot>
        <CreateForm id={LEAVE_CREATE_FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <FormSection
            icon={<CalendarMonthRoundedIcon sx={{ fontSize: 18 }} />}
            title="휴가 정보"
            description="유형 / 기간 / 사유 — 반차는 시작일 하루만 사용."
          >
            <FieldGrid>
              <FieldFull>
                <TextField
                  select
                  fullWidth
                  size="small"
                  label="휴가 유형"
                  required
                  value={values.leaveType}
                  onChange={(e) => form.handleTypeChange(e.target.value as LeaveType)}
                >
                  {LEAVE_TYPE_OPTIONS.map((opt) => (
                    <MenuItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </MenuItem>
                  ))}
                </TextField>
              </FieldFull>
              <TextField
                size="small"
                type="date"
                label="시작일"
                required
                value={values.startDate}
                onChange={(e) => form.handleStartDateChange(e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                size="small"
                type="date"
                label="종료일"
                required
                value={values.endDate}
                onChange={(e) => update('endDate', e.target.value)}
                disabled={form.isHalfDay}
                helperText={form.isHalfDay ? '반차는 종료일이 시작일로 고정됩니다.' : undefined}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <FieldFull>
                <DeductionSummary>
                  <DeductionRow>
                    <DeductionLabel>예상 차감 일수</DeductionLabel>
                    <DeductionValue insufficient={form.insufficientBalance}>
                      {form.deductedDays}일
                    </DeductionValue>
                  </DeductionRow>
                  {form.remainingDays !== undefined && (
                    <DeductionHint>현재 잔여 연차 {form.remainingDays}일</DeductionHint>
                  )}
                  {!form.isDeductible && (
                    <DeductionHint>병가 / 기타는 연차를 차감하지 않습니다.</DeductionHint>
                  )}
                  {form.insufficientBalance && (
                    <DeductionWarning>잔여 연차가 부족하여 신청할 수 없습니다.</DeductionWarning>
                  )}
                </DeductionSummary>
              </FieldFull>
              <FieldFull>
                <TextField
                  fullWidth
                  size="small"
                  label="사유"
                  multiline
                  minRows={3}
                  value={values.reason}
                  onChange={(e) => update('reason', e.target.value)}
                  placeholder="휴가 사유를 입력하세요."
                />
              </FieldFull>
            </FieldGrid>
          </FormSection>

          <FormSection
            icon={<GroupsRoundedIcon sx={{ fontSize: 18 }} />}
            title="결재선"
            description="결재자를 순서대로 추가 — 추가 순서 = 결재 순서."
          >
            <ApprovalLineField
              value={values.approvalLine}
              onChange={(line) => update('approvalLine', line)}
              disabled={form.isSaving}
            />
          </FormSection>
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="휴가 신청 상신"
        message="휴가 신청을 상신하시겠습니까? 신청 즉시 결재가 시작됩니다."
        confirmLabel={form.isSaving ? '상신 중...' : '상신'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
