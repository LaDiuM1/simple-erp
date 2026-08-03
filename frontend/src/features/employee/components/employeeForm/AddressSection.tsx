import TextField from '@mui/material/TextField';
import PlaceRoundedIcon from '@mui/icons-material/PlaceRounded';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import { FormSection } from '@/shared/ui/GenericForm';
import AddressSearchField from '@/shared/ui/AddressSearchField';
import {
  FieldFull,
  FieldGrid,
} from './employeeForm.styles';

interface Props {
  form: EmployeeFormStateBase;
  /** 상세 페이지용 — 주소 검색 버튼 숨김 + 상세 주소 입력 disabled. */
  readOnly?: boolean;
}

export default function AddressSection({ form, readOnly = false }: Props) {
  const { values, update } = form;

  return (
    <FormSection
      icon={<PlaceRoundedIcon sx={{ fontSize: 18 }} />}
      title="주소 정보"
      description="우편번호와 기본 주소 및 상세주소를 입력합니다."
    >
      <FieldGrid>
        <AddressSearchField
          zipCode={values.zipCode}
          roadAddress={values.roadAddress}
          onZipCodeChange={(value) => update('zipCode', value)}
          onRoadAddressChange={(value) => update('roadAddress', value)}
          readOnly={readOnly}
        />
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="상세 주소"
            value={values.detailAddress}
            onChange={(e) => update('detailAddress', e.target.value)}
            disabled={readOnly}
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
