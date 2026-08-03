import { useCallback, useState } from 'react';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import TextField from '@mui/material/TextField';
import type { DaumPostcodeData } from '@/shared/hooks/useDaumPostcode';
import {
  AddressFieldsRoot,
  AddressRoadRow,
  AddressSearchButton,
  AddressSearchRow,
} from './AddressSearchField.styles';
import DaumPostcodeDialog from './DaumPostcodeDialog';

interface Props {
  zipCode: string;
  roadAddress: string;
  onZipCodeChange: (value: string) => void;
  onRoadAddressChange: (value: string) => void;
  readOnly?: boolean;
}

/** 우편번호·기본 주소 입력과 WebView 호환 layer 검색, 실패 시 직접 입력을 묶은 공용 필드. */
export default function AddressSearchField({
  zipCode,
  roadAddress,
  onZipCodeChange,
  onRoadAddressChange,
  readOnly = false,
}: Props) {
  const [open, setOpen] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const manualInput = !readOnly && loadError !== null;

  const handleComplete = useCallback((data: DaumPostcodeData) => {
    onZipCodeChange(data.zonecode);
    onRoadAddressChange(data.roadAddress || data.jibunAddress);
    setLoadError(null);
    setOpen(false);
  }, [onRoadAddressChange, onZipCodeChange]);

  return (
    <AddressFieldsRoot>
      <AddressSearchRow>
        <TextField
          size="small"
          label="우편번호"
          value={zipCode}
          placeholder="00000"
          onChange={(event) => onZipCodeChange(event.target.value)}
          disabled={!manualInput}
          sx={{ flex: 1 }}
        />
        {!readOnly && (
          <AddressSearchButton
            type="button"
            variant="outlined"
            startIcon={<SearchRoundedIcon sx={{ fontSize: 18 }} />}
            onClick={() => setOpen(true)}
          >
            {loadError ? '다시 시도' : '주소 검색'}
          </AddressSearchButton>
        )}
      </AddressSearchRow>
      <AddressRoadRow>
        <TextField
          fullWidth
          size="small"
          label="기본 주소"
          value={roadAddress}
          onChange={(event) => onRoadAddressChange(event.target.value)}
          disabled={!manualInput}
          helperText={manualInput ? loadError : undefined}
        />
      </AddressRoadRow>
      {!readOnly && (
        <DaumPostcodeDialog
          open={open}
          onClose={() => setOpen(false)}
          onComplete={handleComplete}
          onLoadError={setLoadError}
          onLoadSuccess={() => setLoadError(null)}
        />
      )}
    </AddressFieldsRoot>
  );
}
