import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import TuneRoundedIcon from '@mui/icons-material/TuneRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import { MAX_MONEY_AMOUNT } from '@/shared/validation/money';
import { OUTPUT_UNIT_LABELS, type OutputUnit } from '@/features/contract/types';
import type { ContractFormStateBase } from '@/features/contract/hooks/contractFormState';
import { FieldFull, FieldGrid, FieldPair } from './contractForm.styles';

interface Props {
  form: ContractFormStateBase;
}

/**
 * 사양 / 금액 — 출력 (kW·ton) 과 옵션은 같은 모델도 계약마다 달라지는 사양이라
 * 제품 마스터가 아닌 계약 필드로 입력한다. 금액은 초기가 / 최종가 2단 (협상 증감 실무).
 */
export default function SpecAmountSection({ form }: Props) {
  const { values, update, validation, installationBoundary } = form;

  return (
    <FormSection
      icon={<TuneRoundedIcon sx={{ fontSize: 18 }} />}
      title="사양 / 금액"
      description="계약별 설비 사양 (출력 / 옵션) 과 계약금액 (VAT 별도)."
    >
      <FieldGrid>
        <FieldPair>
          <TextField
            size="small"
            type="number"
            label="출력 값"
            disabled={installationBoundary !== null}
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
            disabled={installationBoundary !== null}
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
          type="number"
          label="초기 계약금액 (원)"
          value={values.initialAmount}
          onChange={(e) => update('initialAmount', e.target.value)}
          onBlur={validation.onBlur('initialAmount')}
          error={validation.isInvalid('initialAmount')}
          helperText={validation.errorMessage('initialAmount')}
          placeholder="0"
          slotProps={{ htmlInput: { min: 0, max: MAX_MONEY_AMOUNT } }}
        />
        <TextField
          size="small"
          type="number"
          label="최종 계약금액 (원)"
          required
          value={values.finalAmount}
          onChange={(e) => update('finalAmount', e.target.value)}
          onBlur={validation.onBlur('finalAmount')}
          error={validation.isInvalid('finalAmount')}
          helperText={
            validation.errorMessage('finalAmount')
            ?? '협상 / 옵션 변경이 반영된 최종가 — 미수금 산출 기준.'
          }
          placeholder="0"
          slotProps={{ htmlInput: { min: 0, max: MAX_MONEY_AMOUNT } }}
        />
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="옵션 사양"
            multiline
            minRows={2}
            value={values.optionText}
            onChange={(e) => update('optionText', e.target.value)}
            placeholder="BEVEL, FMC, CHUCK 구성 등 계약별 옵션"
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
