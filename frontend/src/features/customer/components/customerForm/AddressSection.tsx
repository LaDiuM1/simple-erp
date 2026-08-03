import TextField from '@mui/material/TextField';
import PlaceRoundedIcon from '@mui/icons-material/PlaceRounded';
import NotesRoundedIcon from '@mui/icons-material/NotesRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import AddressSearchField from '@/shared/ui/AddressSearchField';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import {
  FieldFull,
  FieldGrid,
} from './customerForm.styles';

interface Props {
  form: CustomerFormStateBase;
  /** 상세 페이지용 — 주소 검색 버튼 숨김 + 모든 입력 컨트롤 disabled. */
  readOnly?: boolean;
}

export default function AddressSection({ form, readOnly = false }: Props) {
  const { values, update } = form;

  return (
    <FormSection
      icon={<PlaceRoundedIcon sx={{ fontSize: 18 }} />}
      title="주소 / 비고"
      description="사업장 주소와 자유 메모."
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
        <FieldFull>
          <TextField
            fullWidth
            multiline
            minRows={3}
            size="small"
            label="비고"
            value={values.note}
            onChange={(e) => update('note', e.target.value)}
            disabled={readOnly}
            slotProps={{
              input: {
                startAdornment: (
                  <NotesRoundedIcon
                    sx={{ fontSize: 18, color: 'text.disabled', alignSelf: 'flex-start', mt: 1, mr: 1 }}
                  />
                ),
              },
            }}
          />
        </FieldFull>
      </FieldGrid>
    </FormSection>
  );
}
