import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import EventNoteRoundedIcon from '@mui/icons-material/EventNoteRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import {
  CONTRACT_STATUS_LABELS,
  type ContractStatus,
} from '@/features/contract/types';
import type { ContractFormStateBase } from '@/features/contract/hooks/contractFormState';
import { FieldFull, FieldGrid } from './contractForm.styles';

interface Props {
  form: ContractFormStateBase;
}

interface DateFieldSpec {
  key:
    | 'contractDate'
    | 'dueDate'
    | 'orderDate'
    | 'expectedArrivalDate'
    | 'arrivalDate'
    | 'installedDate'
    | 'settledDate';
  label: string;
  required?: boolean;
}

/** 마일스톤 일자 — 엑셀에서 한 셀에 혼기되던 이력을 개별 컬럼으로 분리한 설계 그대로 노출. */
const DATE_FIELDS: DateFieldSpec[] = [
  { key: 'contractDate', label: '계약일', required: true },
  { key: 'dueDate', label: '납기일' },
  { key: 'orderDate', label: '발주일' },
  { key: 'expectedArrivalDate', label: '입고 예정일' },
  { key: 'arrivalDate', label: '입고일' },
  { key: 'installedDate', label: '설치 완료일' },
  { key: 'settledDate', label: '정산 완료일' },
];

export default function ScheduleSection({ form }: Props) {
  const { values, update, validation, statusSuggestion, installationBoundary } = form;
  const statusOptions = (Object.keys(CONTRACT_STATUS_LABELS) as ContractStatus[])
    .filter((status) => {
      if (installationBoundary === 'SETTLED') return status === 'SETTLED';
      if (installationBoundary === 'INSTALLED') {
        return status === 'INSTALLED' || status === 'SETTLED';
      }
      return true;
    });

  return (
    <FormSection
      icon={<EventNoteRoundedIcon sx={{ fontSize: 18 }} />}
      title="일정 / 상태"
      description="계약 → 발주 → 입고 → 설치 → 정산 마일스톤 일자와 진행 상태."
    >
      <FieldGrid>
        {DATE_FIELDS.map((f) => (
          <TextField
            key={f.key}
            size="small"
            type="date"
            label={f.label}
            required={f.required}
            disabled={installationBoundary !== null && f.key === 'installedDate'}
            value={values[f.key]}
            onChange={(e) => update(f.key, e.target.value)}
            onBlur={f.required ? validation.onBlur(f.key) : undefined}
            error={f.required ? validation.isInvalid(f.key) : false}
            helperText={f.required ? validation.errorMessage(f.key) : undefined}
            slotProps={{ inputLabel: { shrink: true } }}
          />
        ))}
        <TextField
          select
          size="small"
          label="진행 상태"
          required
          value={values.status}
          onChange={(e) => update('status', e.target.value)}
          helperText={
            statusSuggestion
              ? `입력된 일자 기준 '${CONTRACT_STATUS_LABELS[statusSuggestion]}' 상태가 제안됩니다.`
              : undefined
          }
        >
          {statusOptions.map((s) => (
            <MenuItem key={s} value={s}>
              {CONTRACT_STATUS_LABELS[s]}
            </MenuItem>
          ))}
        </TextField>
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="물류 메모"
            value={values.logisticsNote}
            onChange={(e) => update('logisticsNote', e.target.value)}
            placeholder="컨테이너 구성 (40FR x 2 등) / 선적 특이사항"
            slotProps={{ htmlInput: { maxLength: 255 } }}
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
