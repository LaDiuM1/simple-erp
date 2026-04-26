import CircularProgress from '@mui/material/CircularProgress';
import type { CodeRulePreviewResponse } from '@/features/codeRule/types';
import {
  ErrorText,
  NextCodeLabel,
  NextCodeRow,
  NextCodeValue,
  PanelRoot,
  PanelTitle,
  SamplesLabel,
  SamplesList,
  StatusText,
  SummaryBlock,
} from './PreviewPanel.styles';

interface PreviewPanelProps {
  /** 사람말 요약 — 토큰 문법 없이 풀어 쓴 한 두 줄 설명 */
  summary?: string;
  preview: CodeRulePreviewResponse | null;
  isLoading: boolean;
  errorMessage: string | null;
  needsParentInput: boolean;
  parentCode: string;
}

export default function PreviewPanel({
  summary,
  preview,
  isLoading,
  errorMessage,
  needsParentInput,
  parentCode,
}: PreviewPanelProps) {
  return (
    <PanelRoot>
      <PanelTitle>미리보기</PanelTitle>
      {summary && <SummaryBlock>{summary}</SummaryBlock>}
      {renderBody()}
    </PanelRoot>
  );

  function renderBody() {
    if (errorMessage) {
      return <ErrorText>{errorMessage}</ErrorText>;
    }
    if (needsParentInput && parentCode.trim() === '') {
      return (
        <StatusText>
          미리보기에 사용할 부모 코드를 위에 입력해주세요.
        </StatusText>
      );
    }
    if (!preview) {
      return (
        <StatusText>
          {isLoading ? (
            <>
              <CircularProgress size={12} sx={{ mr: 1, verticalAlign: 'middle' }} />
              미리보기 생성 중...
            </>
          ) : (
            '패턴이 유효해지면 미리보기가 표시됩니다.'
          )}
        </StatusText>
      );
    }
    return (
      <>
        <NextCodeRow>
          <NextCodeLabel>다음 생성될 코드</NextCodeLabel>
          <NextCodeValue>{preview.nextCode}</NextCodeValue>
        </NextCodeRow>
        <SamplesLabel>시퀀스 1~{preview.samples.length} 시뮬레이션</SamplesLabel>
        <SamplesList>
          {preview.samples.map((s, i) => (
            <li key={i}>
              <span className="index">{i + 1}.</span>
              <span>{s}</span>
            </li>
          ))}
        </SamplesList>
      </>
    );
  }
}
