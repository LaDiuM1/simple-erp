import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import FactCheckRoundedIcon from '@mui/icons-material/FactCheckRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import {
  SERVICE_STATUS_LABELS,
  SERVICE_STATUS,
  WARRANTY_DECISION,
  WARRANTY_DECISION_LABELS,
  type ServiceStatus,
  type WarrantyDecision,
} from '@/features/afterService/types';
import type { AfterServiceFormStateBase } from '@/features/afterService/hooks/afterServiceFormState';
import { FieldGrid } from './afterServiceForm.styles';

interface Props {
  form: AfterServiceFormStateBase;
}

/**
 * 처리 / 판정 — 담당 배정, 진행 상태, 유상 / 무상 확정 (설비 보증 기반 제안 helperText) 과 청구액.
 */
export default function ProcessSection({ form }: Props) {
  const { values, update, validation, engineers, warrantySuggestion, handleWarrantyDecisionChange } =
    form;
  const isPaid = values.warrantyDecision === WARRANTY_DECISION.PAID;
  const isCompleted = values.status === SERVICE_STATUS.COMPLETED;

  const handleStatusChange = (status: string) => {
    update('status', status);
    if (status !== SERVICE_STATUS.COMPLETED) update('completedDate', '');
  };

  return (
    <FormSection
      icon={<FactCheckRoundedIcon sx={{ fontSize: 18 }} />}
      title="처리 / 판정"
      description="담당 배정 / 진행 상태 / 유상·무상 확정 — 유상 건은 청구액을 기록해 매출 측면을 남긴다."
    >
      <FieldGrid>
        <TextField
          select
          size="small"
          label="주 담당 엔지니어"
          value={values.assignedEngineerId}
          onChange={(e) => update('assignedEngineerId', e.target.value)}
        >
          <MenuItem value="">미배정</MenuItem>
          {engineers.map((e) => (
            <MenuItem key={e.id} value={String(e.id)}>
              {e.affiliation ? `${e.name} (${e.affiliation})` : e.name}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="진행 상태"
          required
          value={values.status}
          onChange={(e) => handleStatusChange(e.target.value)}
        >
          {(Object.keys(SERVICE_STATUS_LABELS) as ServiceStatus[]).map((s) => (
            <MenuItem key={s} value={s}>
              {SERVICE_STATUS_LABELS[s]}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="유상 / 무상"
          required
          value={values.warrantyDecision}
          onChange={(e) => handleWarrantyDecisionChange(e.target.value)}
          helperText={
            warrantySuggestion && warrantySuggestion.suggestion !== values.warrantyDecision
              ? warrantySuggestion.text
              : undefined
          }
        >
          {(Object.keys(WARRANTY_DECISION_LABELS) as WarrantyDecision[]).map((w) => (
            <MenuItem key={w} value={w}>
              {WARRANTY_DECISION_LABELS[w]}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          size="small"
          type="number"
          label="청구액 (원)"
          value={values.billingAmount}
          onChange={(e) => update('billingAmount', e.target.value)}
          onBlur={validation.onBlur('billingAmount')}
          error={validation.isInvalid('billingAmount')}
          disabled={!isPaid}
          helperText={
            validation.errorMessage('billingAmount')
            ?? (isPaid ? '유상 확정 건 — 고객 청구액을 입력해주세요.' : '유상 확정 시에만 입력합니다.')
          }
          placeholder="0"
          slotProps={{ htmlInput: { min: 1 } }}
        />
        <TextField
          size="small"
          type="date"
          label="완료일"
          required={isCompleted}
          disabled={!isCompleted}
          value={values.completedDate}
          onChange={(e) => update('completedDate', e.target.value)}
          onBlur={validation.onBlur('completedDate')}
          error={validation.isInvalid('completedDate')}
          helperText={
            validation.errorMessage('completedDate')
            ?? (isCompleted ? '접수일 이후의 실제 완료일을 입력해주세요.' : '완료 상태에서만 입력합니다.')
          }
          slotProps={{ inputLabel: { shrink: true } }}
        />
      </FieldGrid>
    </FormSection>
  );
}
