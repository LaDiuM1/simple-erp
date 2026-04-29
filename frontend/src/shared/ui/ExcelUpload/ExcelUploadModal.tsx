import { useEffect, useRef, useState, type DragEvent, type ReactNode } from 'react';
import Dialog from '@mui/material/Dialog';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import DescriptionIcon from '@mui/icons-material/DescriptionOutlined';
import FileUploadIcon from '@mui/icons-material/FileUploadOutlined';
import {
  ModalCloseButton,
  ModalHeader,
  ModalTitle,
} from '@/shared/ui/GenericDetailModal/GenericDetailModal.styles';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { getErrorMessage } from '@/shared/api/error';
import {
  DropZone,
  DropZoneHint,
  DropZoneLabel,
  GuideBox,
  GuideItem,
  HiddenFileInput,
  ModalActionsRow,
  ModalBody,
  ResultErrorCell,
  ResultErrorHeader,
  ResultErrorTable,
  ResultHint,
  ResultSummary,
  TemplateLink,
  UploadModalSecondary,
} from './ExcelUpload.styles';

export interface ExcelRowError {
  rowNum: number;
  field: string | null;
  message: string;
}

export interface ExcelUploadResult {
  totalRows: number;
  successRows: number;
  failedRows: number;
  errors: ExcelRowError[];
}

interface Props {
  open: boolean;
  onClose: () => void;
  /** 모달 제목 — 도메인별로 차이를 두고 싶으면 지정 (예: "고객사 엑셀 업로드"). */
  title?: string;
  /** 양식 다운로드 트리거. */
  onDownloadTemplate?: () => Promise<void> | void;
  /** 업로드 mutation trigger — FormData 받아 ExcelUploadResult 반환. */
  upload: (form: FormData) => { unwrap: () => Promise<ExcelUploadResult> };
  /** mutation 진행 상태. */
  isUploading?: boolean;
  /** 가이드 텍스트 — 도메인별 추가 안내가 있으면 children 으로 주입. 미지정 시 공통 가이드만 노출. */
  extraGuide?: ReactNode;
}

const VALID_EXTENSIONS = ['.xlsx'];

/**
 * 엑셀 일괄 업로드 모달 — 가이드 + 양식 다운로드 + 드래그앤드롭 업로드 + 결과까지 한 화면.
 * 업로드 결과 상태에서는 본문이 결과 표시로 전환되며, 하단 "다른 파일 업로드" 로 다시 IDLE 로 돌아간다.
 */
export default function ExcelUploadModal({
  open,
  onClose,
  title = '엑셀 일괄 업로드',
  onDownloadTemplate,
  upload,
  isUploading,
  extraGuide,
}: Props) {
  const snackbar = useSnackbar();
  const inputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [result, setResult] = useState<ExcelUploadResult | null>(null);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  // 모달이 닫혔다 다시 열리면 결과 / 드래그 상태 초기화.
  useEffect(() => {
    if (open) return;
    const t = window.setTimeout(() => {
      setResult(null);
      setIsDragging(false);
    }, 200);
    return () => window.clearTimeout(t);
  }, [open]);

  const handleFile = async (file: File) => {
    const lower = file.name.toLowerCase();
    if (!VALID_EXTENSIONS.some((ext) => lower.endsWith(ext))) {
      snackbar.error('.xlsx 파일만 업로드할 수 있습니다.');
      return;
    }
    const form = new FormData();
    form.append('file', file);
    try {
      const r = await upload(form).unwrap();
      setResult(r);
      if (r.failedRows === 0) {
        snackbar.success(`${r.successRows}건이 업로드되었습니다.`);
      }
    } catch (err) {
      snackbar.error(getErrorMessage(err, '업로드 중 오류가 발생했습니다.'));
    }
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (file) void handleFile(file);
  };

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleClickDropZone = () => {
    if (isUploading) return;
    inputRef.current?.click();
  };

  const handleSelectFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (file) void handleFile(file);
  };

  const handleReset = () => {
    setResult(null);
  };

  const isFailure = result !== null && result.failedRows > 0;
  const isSuccess = result !== null && result.failedRows === 0;
  const showResult = result !== null;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      slotProps={{ backdrop: { sx: { backgroundColor: 'rgb(0 0 0 / 0.4)' } } }}
      PaperProps={{
        sx: (theme) => ({
          borderRadius: '6px',
          border: `1px solid ${theme.palette.divider}`,
          boxShadow: theme.shadows[4],
          width: '100%',
          maxWidth: 640,
          maxHeight: '80vh',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }),
      }}
    >
      <ModalHeader>
        <ModalTitle>
          {showResult ? (isFailure ? '업로드 실패' : '업로드 완료') : title}
        </ModalTitle>
        <ModalCloseButton size="small" onClick={onClose} aria-label="닫기">
          <CloseRoundedIcon fontSize="small" />
        </ModalCloseButton>
      </ModalHeader>

      <ModalBody>
        {!showResult && (
          <>
            <GuideBox>
              <GuideItem>
                <strong>1.</strong> 아래 양식 파일 다운로드 후 지정된 형식에 맞춰 데이터를 입력해주세요.
              </GuideItem>
              <GuideItem>
                <strong>2.</strong> 작성된 .xlsx 파일을 드래그 앤 드롭 또는 클릭하여 업로드해주세요.
              </GuideItem>
              <GuideItem>
                <strong>3.</strong> 업로드 중 오류 발생 시 에러 내역과 아래 항목을 확인 후 다시 업로드 해주세요.
                <br />
                (오류 발생 시 업로드되지 않습니다)
              </GuideItem>
              {extraGuide}
            </GuideBox>

            {onDownloadTemplate && (
              <TemplateLink type="button" onClick={() => onDownloadTemplate()} disabled={isUploading}>
                <DescriptionIcon fontSize="small" />
                <span>업로드 양식 다운로드</span>
              </TemplateLink>
            )}

            <DropZone
              data-active={isDragging || undefined}
              data-busy={isUploading || undefined}
              onDragEnter={handleDragOver}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              onClick={handleClickDropZone}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleClickDropZone();
                }
              }}
            >
              <FileUploadIcon fontSize="large" />
              <DropZoneLabel>
                {isUploading
                  ? '업로드 중...'
                  : isDragging
                    ? '여기에 놓아 업로드'
                    : '파일을 끌어다 놓거나 클릭해 선택'}
              </DropZoneLabel>
              <DropZoneHint>.xlsx 형식만 지원</DropZoneHint>
              <HiddenFileInput
                ref={inputRef}
                type="file"
                accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                onChange={handleSelectFile}
              />
            </DropZone>
          </>
        )}

        {showResult && result && (
          <>
            <ResultSummary data-tone={isFailure ? 'failure' : 'success'}>
              <span>총 <strong>{result.totalRows}</strong>건</span>
              <span>성공 <strong>{result.successRows}</strong>건</span>
              <span>실패 <strong>{result.failedRows}</strong>건</span>
            </ResultSummary>

            {isFailure && (
              <>
                <ResultHint>
                  오류가 발생한 행을 엑셀에서 수정 후 다시 업로드해 주세요. (저장된 데이터 없음)
                </ResultHint>
                <ResultErrorTable>
                  <ResultErrorHeader>행</ResultErrorHeader>
                  <ResultErrorHeader>항목</ResultErrorHeader>
                  <ResultErrorHeader>메시지</ResultErrorHeader>
                  {result.errors.map((e, i) => (
                    <ErrorRow key={`${e.rowNum}-${i}`} err={e} />
                  ))}
                </ResultErrorTable>
              </>
            )}

            <ModalActionsRow>
              <UploadModalSecondary type="button" onClick={handleReset}>
                다른 파일 업로드
              </UploadModalSecondary>
              <UploadModalSecondary type="button" onClick={onClose} data-primary={isSuccess || undefined}>
                닫기
              </UploadModalSecondary>
            </ModalActionsRow>
          </>
        )}
      </ModalBody>
    </Dialog>
  );
}

function ErrorRow({ err }: { err: ExcelRowError }) {
  return (
    <>
      <ResultErrorCell>{err.rowNum}</ResultErrorCell>
      <ResultErrorCell>{err.field ?? '-'}</ResultErrorCell>
      <ResultErrorCell>{err.message}</ResultErrorCell>
    </>
  );
}
