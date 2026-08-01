/** 게시글과 결재 문서 본문이 공유하는 BE 문자 수 상한. */
export const CONTENT_TEXT_MAX_LENGTH = 4_000;

export function validateContentText(
  value: string,
  label: string,
  required = false,
): string | null {
  if (required && value.trim() === '') return `${label}을 입력해주세요.`;
  if (value.length > CONTENT_TEXT_MAX_LENGTH) {
    return `${label}은 ${CONTENT_TEXT_MAX_LENGTH.toLocaleString('ko-KR')}자 이하로 입력해주세요.`;
  }
  return null;
}
