import TextField from '@mui/material/TextField';
import PlaceRoundedIcon from '@mui/icons-material/PlaceRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import NotesRoundedIcon from '@mui/icons-material/NotesRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import {
  AddressSearchButton,
  AddressSearchRow,
  FieldFull,
  FieldGrid,
} from './customerForm.styles';

interface Props {
  form: CustomerFormStateBase;
  /** 상세 페이지용 — 주소 검색 버튼 숨김 + 모든 입력 컨트롤 disabled. */
  readOnly?: boolean;
}

export default function AddressSection({ form, readOnly = false }: Props) {
  const { values, update, handleAddressSearch } = form;

  return (
    <FormSection
      icon={<PlaceRoundedIcon sx={{ fontSize: 18 }} />}
      title="주소 / 비고"
      description="사업장 주소와 자유 메모."
    >
      <FieldGrid>
        <AddressSearchRow>
          <TextField
            size="small"
            label="우편번호"
            value={values.zipCode}
            placeholder="00000"
            disabled
            sx={{ flex: 1 }}
          />
          {!readOnly && (
            <AddressSearchButton
              type="button"
              variant="outlined"
              startIcon={<SearchRoundedIcon sx={{ fontSize: 18 }} />}
              onClick={handleAddressSearch}
            >
              주소 검색
            </AddressSearchButton>
          )}
        </AddressSearchRow>
        <FieldFull>
          <TextField
            fullWidth
            size="small"
            label="기본 주소"
            value={values.roadAddress}
            disabled
          />
        </FieldFull>
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
