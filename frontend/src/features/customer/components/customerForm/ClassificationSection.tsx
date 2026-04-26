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

interface Props {
  form: CustomerFormStateBase;
  /** 상세 페이지용 — 모든 입력 컨트롤을 disabled 처리. */
  readOnly?: boolean;
}

export default function ClassificationSection({ form, readOnly = false }: Props) {
  const { values, update } = form;

  return (
    <FormSection
      icon={<CategoryRoundedIcon sx={{ fontSize: 18 }} />}
      title="분류 / 거래"
      description="고객 분류 / 거래 상태 / 거래 시작일 / 업태 / 업종."
    >
      <FieldGrid>
        <TextField
          select
          size="small"
          label="고객 분류"
          required
          value={values.type}
          onChange={(e) => update('type', e.target.value as CustomerType)}
          disabled={readOnly}
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
          disabled={readOnly}
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
          disabled={readOnly}
        />
        <TextField
          size="small"
          label="업태"
          value={values.bizType}
          onChange={(e) => update('bizType', e.target.value)}
          placeholder="제조 / 도소매 / 서비스 등"
          disabled={readOnly}
        />
        <TextField
          size="small"
          label="업종"
          value={values.bizItem}
          onChange={(e) => update('bizItem', e.target.value)}
          placeholder="전자제품 / 소프트웨어 등"
          disabled={readOnly}
        />
      </FieldGrid>
    </FormSection>
  );
}
