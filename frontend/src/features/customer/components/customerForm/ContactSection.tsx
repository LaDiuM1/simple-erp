import TextField from '@mui/material/TextField';
import ContactPhoneRoundedIcon from '@mui/icons-material/ContactPhoneRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import { FieldGrid } from './customerForm.styles';

interface Props {
  form: CustomerFormStateBase;
  /** 상세 페이지용 — 모든 입력 컨트롤을 disabled 처리. */
  readOnly?: boolean;
}

export default function ContactSection({ form, readOnly = false }: Props) {
  const { values, update, validation } = form;

  return (
    <FormSection
      icon={<ContactPhoneRoundedIcon sx={{ fontSize: 18 }} />}
      title="연락처"
      description="대표 전화 / 팩스 / 이메일 / 홈페이지."
    >
      <FieldGrid>
        <TextField
          size="small"
          type="tel"
          label="대표 전화"
          value={values.phone}
          onChange={(e) => update('phone', e.target.value)}
          placeholder="02-0000-0000"
          disabled={readOnly}
        />
        <TextField
          size="small"
          type="tel"
          label="팩스"
          value={values.fax}
          onChange={(e) => update('fax', e.target.value)}
          disabled={readOnly}
        />
        <TextField
          size="small"
          type="email"
          label="대표 이메일"
          value={values.email}
          onChange={(e) => update('email', e.target.value)}
          onBlur={validation.onBlur('email')}
          error={!readOnly && validation.isInvalid('email')}
          helperText={!readOnly ? validation.errorMessage('email') : undefined}
          placeholder="contact@example.com"
          disabled={readOnly}
        />
        <TextField
          size="small"
          label="홈페이지"
          value={values.website}
          onChange={(e) => update('website', e.target.value)}
          onBlur={validation.onBlur('website')}
          error={!readOnly && validation.isInvalid('website')}
          helperText={!readOnly ? validation.errorMessage('website') : undefined}
          placeholder="https://example.com"
          disabled={readOnly}
        />
      </FieldGrid>
    </FormSection>
  );
}
