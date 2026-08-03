import { useCallback, useEffect, useRef, useState } from 'react';
import CloseIcon from '@mui/icons-material/Close';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import { useDaumPostcode, type DaumPostcodeData } from '@/shared/hooks/useDaumPostcode';
import LoadingSpinner from '@/shared/ui/feedback/LoadingSpinner';
import {
  DialogBody,
  DialogStatus,
  DialogTitleRow,
  PostcodeContainer,
} from './AddressSearchField.styles';

interface Props {
  open: boolean;
  onClose: () => void;
  onComplete: (data: DaumPostcodeData) => void;
  onLoadError: (message: string) => void;
  onLoadSuccess: () => void;
}

const TITLE_ID = 'address-search-dialog-title';

/** popup 대신 현재 문서 안에 Kakao Postcode iframe 을 embed 하는 WebView 호환 주소 검색 Dialog. */
export default function DaumPostcodeDialog({
  open,
  onClose,
  onComplete,
  onLoadError,
  onLoadSuccess,
}: Props) {
  const [container, setContainer] = useState<HTMLDivElement | null>(null);
  const callbacksRef = useRef({ onClose, onComplete, onLoadError, onLoadSuccess });
  const [attempt, setAttempt] = useState(0);
  const { embedPostcode, isLoading, error } = useDaumPostcode();

  useEffect(() => {
    callbacksRef.current = { onClose, onComplete, onLoadError, onLoadSuccess };
  }, [onClose, onComplete, onLoadError, onLoadSuccess]);

  const mountPostcode = useCallback(async (container: HTMLElement, signal: AbortSignal) => {
    container.replaceChildren();
    try {
      const mounted = await embedPostcode(container, {
        onComplete: (data) => callbacksRef.current.onComplete(data),
        onClose: () => callbacksRef.current.onClose(),
        signal,
      });
      if (mounted) callbacksRef.current.onLoadSuccess();
    } catch {
      // 훅의 error 상태와 아래 effect 가 화면·직접 입력 fallback 을 함께 갱신한다.
    }
  }, [embedPostcode]);

  useEffect(() => {
    if (!open || !container) return undefined;
    const controller = new AbortController();
    void mountPostcode(container, controller.signal);
    return () => {
      controller.abort();
      container.replaceChildren();
    };
  }, [attempt, container, mountPostcode, open]);

  useEffect(() => {
    if (error) callbacksRef.current.onLoadError(error);
  }, [error]);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      aria-labelledby={TITLE_ID}
    >
      <DialogTitle id={TITLE_ID} component="div">
        <DialogTitleRow>
          <Typography component="h2" sx={{ fontSize: '1rem', fontWeight: 600 }}>
            주소 검색
          </Typography>
          <IconButton onClick={onClose} aria-label="주소 검색 닫기" size="small">
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitleRow>
      </DialogTitle>
      <DialogBody dividers>
        <PostcodeContainer ref={setContainer} role="region" aria-label="주소 검색 결과" />
        {isLoading && (
          <DialogStatus role="status" aria-live="polite">
            <LoadingSpinner />
            <Typography color="text.secondary" variant="body2">
              주소 검색을 불러오는 중입니다.
            </Typography>
          </DialogStatus>
        )}
        {error && !isLoading && (
          <DialogStatus role="alert">
            <Typography color="text.secondary" variant="body2">
              {error}
            </Typography>
            <Button variant="outlined" onClick={() => setAttempt((value) => value + 1)}>
              다시 시도
            </Button>
            <Typography color="text.disabled" variant="caption">
              창을 닫으면 우편번호와 기본 주소를 직접 입력할 수 있습니다.
            </Typography>
          </DialogStatus>
        )}
      </DialogBody>
    </Dialog>
  );
}
