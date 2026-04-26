import TextField from '@mui/material/TextField';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import CodeField from '@/features/codeRule/components/CodeField/CodeField';
import { CODE_RULE_TARGET } from '@/features/codeRule/types';
import { useCheckCustomerCodeAvailabilityQuery } from '@/features/customer/api/customerApi';
import {
  availabilityStatusText,
  renderAvailabilityIcon,
} from '@/features/customer/validation/customerFormValidation';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import { FieldGrid } from './customerForm.styles';

interface Props {
  form: CustomerFormStateBase;
  /** 'create' 면 CodeField 가 채번 규칙 기반으로 표시. 'edit' 면 단순 readonly. */
  mode: 'create' | 'edit';
}

export default function IdentitySection({ form, mode }: Props) {
  const { values, update, validation, bizRegNoStatus } = form;

  return (
    <FormSection
      icon={<BusinessRoundedIcon sx={{ fontSize: 18 }} />}
      title="기본 정보"
      description="고객사 식별 정보 — 코드 / 명칭 / 사업자 정보를 입력합니다."
    >
      <FieldGrid>
        <CodeField
          target={CODE_RULE_TARGET.CUSTOMER}
          value={values.code}
          onChange={(v) => update('code', v)}
          mode={mode}
          disabled={mode === 'edit'}
          label="고객사 코드"
          useCheckAvailability={useCheckCustomerCodeAvailabilityQuery}
        />
        <TextField
          size="small"
          label="고객사명"
          required
          value={values.name}
          onChange={(e) => update('name', e.target.value)}
          onBlur={validation.onBlur('name')}
          error={validation.isInvalid('name')}
          helperText={validation.errorMessage('name')}
        />
        <TextField
          size="small"
          label="영문 고객사명"
          value={values.nameEn}
          onChange={(e) => update('nameEn', e.target.value)}
          placeholder="Daesung Co., Ltd."
        />
        <TextField
          size="small"
          label="대표자명"
          value={values.representative}
          onChange={(e) => update('representative', e.target.value)}
        />
        <TextField
          size="small"
          label="사업자등록번호"
          value={values.bizRegNo}
          onChange={(e) => update('bizRegNo', e.target.value)}
          onBlur={validation.onBlur('bizRegNo')}
          error={validation.isInvalid('bizRegNo') || bizRegNoStatus === 'taken'}
          helperText={
            validation.errorMessage('bizRegNo')
            ?? availabilityStatusText(bizRegNoStatus, 'bizRegNo')
          }
          placeholder="123-45-67890"
          slotProps={{
            input: { endAdornment: renderAvailabilityIcon(bizRegNoStatus) },
          }}
        />
        <TextField
          size="small"
          label="법인등록번호"
          value={values.corpRegNo}
          onChange={(e) => update('corpRegNo', e.target.value)}
        />
      </FieldGrid>
    </FormSection>
  );
}
