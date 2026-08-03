import TextField from '@mui/material/TextField';
import DescriptionRoundedIcon from '@mui/icons-material/DescriptionRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import CodeField from '@/features/codeRule/components/CodeField/CodeField';
import { CODE_RULE_TARGET } from '@/features/codeRule/types';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField';
import ContractEmployeeSelectField from '@/features/contract/components/ContractEmployeeSelectField';
import ProductSelectField from '@/features/product/components/ProductSelectField/ProductSelectField';
import type { ContractFormStateBase } from '@/features/contract/hooks/contractFormState';
import { FieldGrid } from './contractForm.styles';

interface Props {
  form: ContractFormStateBase;
  /** 'create' 면 CodeField 가 채번 규칙 기반으로 표시. 'edit' 면 단순 readonly. */
  mode: 'create' | 'edit';
}

export default function BasicSection({ form, mode }: Props) {
  const { values, update, validation, supplierName } = form;

  return (
    <FormSection
      icon={<DescriptionRoundedIcon sx={{ fontSize: 18 }} />}
      title="기본 정보"
      description="계약 식별 정보 — 계약 번호 / 고객사 / 계약자 / 설비."
    >
      <FieldGrid>
        <CodeField
          target={CODE_RULE_TARGET.CONTRACT}
          value={values.contractNo}
          onChange={(v) => update('contractNo', v)}
          mode={mode}
          disabled={mode !== 'create'}
          label="계약 번호"
        />
        <TextField
          size="small"
          label="CRETOP 등급"
          value={values.cretopGrade}
          onChange={(e) => update('cretopGrade', e.target.value)}
          placeholder="계약 시점 신용등급 (소문자 = 모의등급)"
          slotProps={{ htmlInput: { maxLength: 10 } }}
        />
        <CustomerSelectField
          required
          value={values.customerId}
          valueLabel={values.customerName}
          onChange={(id, name) => {
            update('customerId', id);
            update('customerName', name);
          }}
          helperText={validation.errorMessage('customerId')}
        />
        <ContractEmployeeSelectField
          label="계약자"
          required
          value={values.employeeId}
          valueLabel={values.employeeName}
          onChange={(id, name) => {
            update('employeeId', id);
            update('employeeName', name);
          }}
          helperText={validation.errorMessage('employeeId')}
        />
        <ProductSelectField
          required
          value={values.productId}
          valueLabel={values.productModelName}
          onChange={(id, name) => {
            update('productId', id);
            update('productModelName', name);
          }}
          helperText={validation.errorMessage('productId')}
        />
        <TextField
          size="small"
          label="공급사"
          value={supplierName}
          disabled
          helperText="제품 모델 선택 시 해당 공급사가 자동 저장됩니다."
        />
      </FieldGrid>
    </FormSection>
  );
}
