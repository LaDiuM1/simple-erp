import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import SupportAgentRoundedIcon from '@mui/icons-material/SupportAgentRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import CodeField from '@/features/codeRule/components/CodeField/CodeField';
import { CODE_RULE_TARGET } from '@/features/codeRule/types';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField';
import EquipmentSelectField from '@/features/equipment/components/EquipmentSelectField/EquipmentSelectField';
import { SERVICE_TYPE_LABELS, type ServiceType } from '@/features/afterService/types';
import type { AfterServiceFormStateBase } from '@/features/afterService/hooks/afterServiceFormState';
import { FieldFull, FieldGrid } from './afterServiceForm.styles';

interface Props {
  form: AfterServiceFormStateBase;
  /** 'create' 면 CodeField 가 채번 규칙 기반으로 표시. 'edit' 면 단순 readonly. */
  mode: 'create' | 'edit';
}

export default function ReceptionSection({ form, mode }: Props) {
  const { values, update, validation, handleCustomerChange } = form;

  return (
    <FormSection
      icon={<SupportAgentRoundedIcon sx={{ fontSize: 18 }} />}
      title="접수 정보"
      description="접수번호 / 고객사 / 대상 설비 / 증상 — 대장 미등록 설비는 설비 없이 접수 가능."
    >
      <FieldGrid>
        <CodeField
          target={CODE_RULE_TARGET.AFTER_SERVICE}
          value={values.receiptNo}
          onChange={(v) => update('receiptNo', v)}
          mode={mode}
          disabled={mode !== 'create'}
          label="접수번호"
        />
        <TextField
          size="small"
          type="date"
          label="접수일"
          required
          value={values.receivedDate}
          onChange={(e) => update('receivedDate', e.target.value)}
          onBlur={validation.onBlur('receivedDate')}
          error={validation.isInvalid('receivedDate')}
          helperText={validation.errorMessage('receivedDate')}
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <CustomerSelectField
          required
          value={values.customerId}
          valueLabel={values.customerName}
          onChange={handleCustomerChange}
          helperText={validation.errorMessage('customerId')}
        />
        <EquipmentSelectField
          customerId={values.customerId}
          value={values.equipmentId}
          valueLabel={values.equipmentLabel}
          disabled={values.customerId === ''}
          onChange={(id, name) => {
            update('equipmentId', id);
            update('equipmentLabel', name);
          }}
          helperText="설비 연결 시 보증 기준 유상 / 무상이 자동 제안됩니다."
        />
        <TextField
          select
          size="small"
          label="AS 유형"
          required
          value={values.type}
          onChange={(e) => update('type', e.target.value)}
        >
          {(Object.keys(SERVICE_TYPE_LABELS) as ServiceType[]).map((t) => (
            <MenuItem key={t} value={t}>
              {SERVICE_TYPE_LABELS[t]}
            </MenuItem>
          ))}
        </TextField>
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="증상 / 요청 내용"
            multiline
            minRows={2}
            value={values.symptom}
            onChange={(e) => update('symptom', e.target.value)}
            placeholder="레이저 출력 저하, 절단면 불량 등"
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
