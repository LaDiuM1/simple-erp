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
  /** 'create' 면 CodeField 가 채번 규칙 기반으로 표시. 'edit' / 'detail' 이면 단순 readonly. */
  mode: 'create' | 'edit' | 'detail';
  /** 상세 페이지용 — 모든 입력 컨트롤을 disabled 처리하여 읽기 전용 표시. */
  readOnly?: boolean;
}

export default function IdentitySection({ form, mode, readOnly = false }: Props) {
  const { values, update, validation, bizRegNoStatus } = form;
  const codeFieldMode = mode === 'create' ? 'create' : 'edit';

  return (
    <FormSection
      icon={<BusinessRoundedIcon sx={{ fontSize: 18 }} />}
      title="기본 정보"
      description="고객사 식별 정보 — 코드 / 명칭 / 사업자 정보."
    >
      <FieldGrid>
        <CodeField
          target={CODE_RULE_TARGET.CUSTOMER}
          value={values.code}
          onChange={(v) => update('code', v)}
          mode={codeFieldMode}
          disabled={mode !== 'create'}
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
          error={!readOnly && validation.isInvalid('name')}
          helperText={!readOnly ? validation.errorMessage('name') : undefined}
          disabled={readOnly}
        />
        <TextField
          size="small"
          label="영문 고객사명"
          value={values.nameEn}
          onChange={(e) => update('nameEn', e.target.value)}
          placeholder="Daesung Co., Ltd."
          disabled={readOnly}
        />
        <TextField
          size="small"
          label="대표자명"
          value={values.representative}
          onChange={(e) => update('representative', e.target.value)}
          disabled={readOnly}
        />
        <TextField
          size="small"
          label="사업자등록번호"
          value={values.bizRegNo}
          onChange={(e) => update('bizRegNo', e.target.value)}
          onBlur={validation.onBlur('bizRegNo')}
          error={!readOnly && (validation.isInvalid('bizRegNo') || bizRegNoStatus === 'taken')}
          helperText={
            !readOnly
              ? validation.errorMessage('bizRegNo')
                ?? availabilityStatusText(bizRegNoStatus, 'bizRegNo')
              : undefined
          }
          placeholder="123-45-67890"
          disabled={readOnly}
          slotProps={{
            input: { endAdornment: !readOnly ? renderAvailabilityIcon(bizRegNoStatus) : undefined },
          }}
        />
        <TextField
          size="small"
          label="법인등록번호"
          value={values.corpRegNo}
          onChange={(e) => update('corpRegNo', e.target.value)}
          disabled={readOnly}
        />
      </FieldGrid>
    </FormSection>
  );
}
