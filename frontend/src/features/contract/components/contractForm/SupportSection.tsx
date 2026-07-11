import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import AccountBalanceRoundedIcon from '@mui/icons-material/AccountBalanceRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import {
  SUPPORT_PROGRAM_STATUS_LABELS,
  type SupportProgramStatus,
} from '@/features/contract/types';
import type { ContractFormStateBase } from '@/features/contract/hooks/contractFormState';
import { FieldGrid } from './contractForm.styles';

interface Props {
  form: ContractFormStateBase;
}

/**
 * 정부 지원사업 — 선정 여부가 계약 성사 / 대금 스케줄 (지원금 입금 연동) 에 직결되는 실무 반영.
 */
export default function SupportSection({ form }: Props) {
  const { values, update } = form;

  return (
    <FormSection
      icon={<AccountBalanceRoundedIcon sx={{ fontSize: 18 }} />}
      title="정부 지원사업"
      description="안전동행 / 스마트공방 등 연계 지원사업과 진행 상태."
    >
      <FieldGrid>
        <TextField
          size="small"
          label="지원사업명"
          value={values.supportProgramName}
          onChange={(e) => update('supportProgramName', e.target.value)}
          placeholder="안전동행 지원사업"
          slotProps={{ htmlInput: { maxLength: 200 } }}
        />
        <TextField
          select
          size="small"
          label="진행 상태"
          value={values.supportProgramStatus}
          onChange={(e) => update('supportProgramStatus', e.target.value)}
        >
          {(Object.keys(SUPPORT_PROGRAM_STATUS_LABELS) as SupportProgramStatus[]).map((s) => (
            <MenuItem key={s} value={s}>
              {SUPPORT_PROGRAM_STATUS_LABELS[s]}
            </MenuItem>
          ))}
        </TextField>
      </FieldGrid>
    </FormSection>
  );
}
