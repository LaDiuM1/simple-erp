import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import PrecisionManufacturingRoundedIcon from '@mui/icons-material/PrecisionManufacturingRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField';
import ProductSelectField from '@/features/product/components/ProductSelectField/ProductSelectField';
import { OUTPUT_UNIT_LABELS, type OutputUnit } from '@/features/equipment/types';
import type { EquipmentFormStateBase } from '@/features/equipment/hooks/equipmentFormState';
import { FieldGrid, FieldPair } from './equipmentForm.styles';

interface Props {
  form: EquipmentFormStateBase;
}

export default function BasicSection({ form }: Props) {
  const { values, update, validation, supplierName, contractLinked } = form;

  return (
    <FormSection
      icon={<PrecisionManufacturingRoundedIcon sx={{ fontSize: 18 }} />}
      title="설비 정보"
      description="설치된 설비의 식별 정보 — 고객사 / 모델 / 시리얼 / 설치 주소."
    >
      <FieldGrid>
        <CustomerSelectField
          disabled={contractLinked}
          required
          value={values.customerId}
          valueLabel={values.customerName}
          onChange={(id, name) => {
            update('customerId', id);
            update('customerName', name);
          }}
          helperText={validation.errorMessage('customerId')}
        />
        <ProductSelectField
          disabled={contractLinked}
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
        <FieldPair>
          <TextField
            size="small"
            type="number"
            label="출력 값"
            disabled={contractLinked}
            value={values.outputValue}
            onChange={(e) => update('outputValue', e.target.value)}
            onBlur={validation.onBlur('outputValue')}
            error={validation.isInvalid('outputValue')}
            helperText={validation.errorMessage('outputValue')}
            placeholder="12"
            slotProps={{ htmlInput: { min: 0 } }}
          />
          <TextField
            select
            size="small"
            label="출력 단위"
            disabled={contractLinked}
            value={values.outputUnit}
            onChange={(e) => update('outputUnit', e.target.value)}
            onBlur={validation.onBlur('outputUnit')}
            error={validation.isInvalid('outputUnit')}
            helperText={validation.errorMessage('outputUnit')}
          >
            <MenuItem value="">선택 안 함</MenuItem>
            {(Object.keys(OUTPUT_UNIT_LABELS) as OutputUnit[]).map((unit) => (
              <MenuItem key={unit} value={unit}>
                {OUTPUT_UNIT_LABELS[unit]}
              </MenuItem>
            ))}
          </TextField>
        </FieldPair>
        <TextField
          size="small"
          label="시리얼 번호"
          value={values.serialNo}
          onChange={(e) => update('serialNo', e.target.value)}
          placeholder="명판 번호 — 미확인 시 비워둠"
          slotProps={{ htmlInput: { maxLength: 100 } }}
        />
        <TextField
          size="small"
          label="설치 주소"
          value={values.installAddress}
          onChange={(e) => update('installAddress', e.target.value)}
          placeholder="고객사 주소와 다르면 실제 설치 위치 입력"
          slotProps={{ htmlInput: { maxLength: 255 } }}
        />
      </FieldGrid>
    </FormSection>
  );
}
