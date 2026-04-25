import TextField from '@mui/material/TextField';
import PlaceRoundedIcon from '@mui/icons-material/PlaceRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import FormSection from './FormSection';
import {
  AddressSearchButton,
  AddressSearchRow,
  FieldFull,
  FieldGrid,
} from './employeeForm.styles';

export default function AddressSection({ form }: { form: EmployeeFormStateBase }) {
  const { values, update, handleAddressSearch } = form;

  return (
    <FormSection
      icon={<PlaceRoundedIcon sx={{ fontSize: 18 }} />}
      title="주소 정보"
      description="우편번호와 기본 주소 및 상세주소를 입력합니다."
    >
      <FieldGrid>
        <AddressSearchRow>
          <TextField
            size="small"
            label="우편번호"
            value={values.zipCode}
            placeholder="00000"
            slotProps={{ input: { readOnly: true } }}
            sx={{ flex: 1 }}
          />
          <AddressSearchButton
            type="button"
            variant="outlined"
            startIcon={<SearchRoundedIcon sx={{ fontSize: 18 }} />}
            onClick={handleAddressSearch}
          >
            주소 검색
          </AddressSearchButton>
        </AddressSearchRow>
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="기본 주소"
            value={values.roadAddress}
            slotProps={{ input: { readOnly: true } }}
          />
        </FieldFull>
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="상세 주소"
            value={values.detailAddress}
            onChange={(e) => update('detailAddress', e.target.value)}
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
