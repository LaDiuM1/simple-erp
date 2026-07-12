import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import VerifiedUserRoundedIcon from '@mui/icons-material/VerifiedUserRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import type { EquipmentFormStateBase } from '@/features/equipment/hooks/equipmentFormState';
import { FieldFull, FieldGrid } from './equipmentForm.styles';

interface Props {
  form: EquipmentFormStateBase;
}

/**
 * 설치 / 보증 — 발진기 / 그외 이원 보증 (계약서 기준 개월 수). 만료일은 기산일 + 개월로
 * 서버가 파생 저장하며 AS 접수 시 유상 / 무상 자동 판정의 근거가 된다.
 */
export default function WarrantySection({ form }: Props) {
  const { values, update, validation } = form;

  return (
    <FormSection
      icon={<VerifiedUserRoundedIcon sx={{ fontSize: 18 }} />}
      title="설치 / 보증"
      description="설치 일자와 부위별 보증 (발진기 / 그외 무상 AS) — AS 유상 / 무상 판정 기준."
    >
      <FieldGrid>
        <TextField
          size="small"
          type="date"
          label="설치일"
          value={values.installedDate}
          onChange={(e) => update('installedDate', e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <TextField
          size="small"
          type="date"
          label="설치완료확인서 일자"
          value={values.confirmedDate}
          onChange={(e) => update('confirmedDate', e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <TextField
          size="small"
          type="date"
          label="보증 기산일"
          value={values.warrantyStartDate}
          onChange={(e) => update('warrantyStartDate', e.target.value)}
          helperText="보통 설치완료확인서 일자 기준 — 계약서 조건에 따름."
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <TextField
          select
          size="small"
          label="보증보험"
          value={values.warrantyInsurance}
          onChange={(e) => update('warrantyInsurance', e.target.value)}
        >
          <MenuItem value="false">미가입</MenuItem>
          <MenuItem value="true">가입</MenuItem>
        </TextField>
        <TextField
          size="small"
          type="number"
          label="발진기 보증 (개월)"
          value={values.oscillatorWarrantyMonths}
          onChange={(e) => update('oscillatorWarrantyMonths', e.target.value)}
          onBlur={validation.onBlur('oscillatorWarrantyMonths')}
          error={validation.isInvalid('oscillatorWarrantyMonths')}
          helperText={validation.errorMessage('oscillatorWarrantyMonths')}
          placeholder="24 ~ 60"
          slotProps={{ htmlInput: { min: 0 } }}
        />
        <TextField
          size="small"
          type="number"
          label="그외 무상 AS (개월)"
          value={values.generalWarrantyMonths}
          onChange={(e) => update('generalWarrantyMonths', e.target.value)}
          onBlur={validation.onBlur('generalWarrantyMonths')}
          error={validation.isInvalid('generalWarrantyMonths')}
          helperText={validation.errorMessage('generalWarrantyMonths')}
          placeholder="0 ~ 30"
          slotProps={{ htmlInput: { min: 0 } }}
        />
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="비고"
            multiline
            minRows={2}
            value={values.note}
            onChange={(e) => update('note', e.target.value)}
            placeholder="설치 특이사항 / 보증 조건 메모"
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
