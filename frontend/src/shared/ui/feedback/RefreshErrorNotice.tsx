import Button from '@mui/material/Button';
import { getErrorMessage } from '@/shared/api/error';
import { RefreshAlert } from './RefreshErrorNotice.styles';

interface Props {
  error?: unknown;
  onRetry: () => void;
}

const DEFAULT_MESSAGE = '최신 결과를 불러오지 못했습니다. 현재 결과를 유지합니다.';

/** 현재 결과는 유효하지만 백그라운드 갱신만 실패했을 때 사용하는 비차단 안내. */
export default function RefreshErrorNotice({ error, onRetry }: Props) {
  return (
    <RefreshAlert severity="warning" action={<Button onClick={onRetry}>다시 시도</Button>}>
      {getErrorMessage(error, DEFAULT_MESSAGE)}
    </RefreshAlert>
  );
}
