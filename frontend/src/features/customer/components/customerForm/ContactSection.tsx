import TextField from '@mui/material/TextField';
import ContactPhoneRoundedIcon from '@mui/icons-material/ContactPhoneRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import { FieldGrid } from './customerForm.styles';

export default function ContactSection({ form }: { form: CustomerFormStateBase }) {
  const { values, update, validation } = form;

  return (
    <FormSection
      icon={<ContactPhoneRoundedIcon sx={{ fontSize: 18 }} />}
      title="연락처"
      description="대표 전화 / 팩스 / 이메일 / 홈페이지를 입력합니다."
    >
      <FieldGrid>
        <TextField
          size="small"
          type="tel"
          label="대표 전화"
          value={values.phone}
          onChange={(e) => update('phone', e.target.value)}
          placeholder="02-0000-0000"
        />
        <TextField
          size="small"
          type="tel"
          label="팩스"
          value={values.fax}
          onChange={(e) => update('fax', e.target.value)}
        />
        <TextField
          size="small"
          type="email"
          label="대표 이메일"
          value={values.email}
          onChange={(e) => update('email', e.target.value)}
          onBlur={validation.onBlur('email')}
          error={validation.isInvalid('email')}
          helperText={validation.errorMessage('email')}
          placeholder="contact@example.com"
        />
        <TextField
          size="small"
          label="홈페이지"
          value={values.website}
          onChange={(e) => update('website', e.target.value)}
          onBlur={validation.onBlur('website')}
          error={validation.isInvalid('website')}
          helperText={validation.errorMessage('website')}
          placeholder="https://example.com"
        />
      </FieldGrid>
    </FormSection>
  );
}
