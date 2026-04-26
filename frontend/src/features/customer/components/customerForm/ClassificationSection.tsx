import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import CategoryRoundedIcon from '@mui/icons-material/CategoryRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import {
  CUSTOMER_STATUS_OPTIONS,
  CUSTOMER_TYPE_OPTIONS,
  type CustomerStatus,
  type CustomerType,
} from '@/features/customer/types';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import { FieldGrid } from './customerForm.styles';

export default function ClassificationSection({ form }: { form: CustomerFormStateBase }) {
  const { values, update } = form;

  return (
    <FormSection
      icon={<CategoryRoundedIcon sx={{ fontSize: 18 }} />}
      title="분류 / 거래"
      description="고객 분류와 거래 상태, 거래 시작일과 업태 / 업종을 지정합니다."
    >
      <FieldGrid>
        <TextField
          select
          size="small"
          label="고객 분류"
          required
          value={values.type}
          onChange={(e) => update('type', e.target.value as CustomerType)}
        >
          {CUSTOMER_TYPE_OPTIONS.map((opt) => (
            <MenuItem key={opt.value} value={opt.value}>
              {opt.label}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="거래 상태"
          required
          value={values.status}
          onChange={(e) => update('status', e.target.value as CustomerStatus)}
        >
          {CUSTOMER_STATUS_OPTIONS.map((opt) => (
            <MenuItem key={opt.value} value={opt.value}>
              {opt.label}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          size="small"
          type="date"
          label="거래 시작일"
          value={values.tradeStartDate}
          onChange={(e) => update('tradeStartDate', e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <TextField
          size="small"
          label="업태"
          value={values.bizType}
          onChange={(e) => update('bizType', e.target.value)}
          placeholder="제조 / 도소매 / 서비스 등"
        />
        <TextField
          size="small"
          label="업종"
          value={values.bizItem}
          onChange={(e) => update('bizItem', e.target.value)}
          placeholder="전자제품 / 소프트웨어 등"
        />
      </FieldGrid>
    </FormSection>
  );
}
