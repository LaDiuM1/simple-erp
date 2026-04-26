import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import type { CodeRuleFormValues } from '@/features/codeRule/types';

const TOKEN_REGEX = /\{([A-Z]+)(?::(\d+))?}/g;
const KNOWN_TOKENS = new Set(['PREFIX', 'YYYY', 'YY', 'MM', 'DD', 'SEQ', 'PARENT']);
const MAX_SEQ_LENGTH = 18;

/**
 * 클라이언트 사전 검증 — BE PatternCompiler.validate 와 동일한 룰을 미러.
 * 서버가 최종 검증이지만 즉시 피드백 위해 동일 로직 보유.
 */
export function validatePattern(pattern: string, defaultSeqLength: number): string | null {
  const trimmed = pattern.trim();
  if (trimmed === '') return '패턴을 입력해주세요.';
  if (trimmed.length > 200) return '패턴은 200자 이하여야 합니다.';

  let hasSeq = false;
  TOKEN_REGEX.lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = TOKEN_REGEX.exec(trimmed)) !== null) {
    const [, name, arg] = match;
    if (!KNOWN_TOKENS.has(name)) {
      return `알 수 없는 토큰: {${name}}`;
    }
    if (arg !== undefined && name !== 'SEQ') {
      return `{${name}} 토큰은 자릿수 인자를 가질 수 없습니다.`;
    }
    if (name === 'SEQ') {
      hasSeq = true;
      if (arg !== undefined) {
        const n = Number(arg);
        if (n < 1 || n > MAX_SEQ_LENGTH) {
          return `SEQ 자릿수는 1~${MAX_SEQ_LENGTH} 사이여야 합니다.`;
        }
      }
    }
  }
  if (!hasSeq) {
    return '{SEQ} 토큰을 최소 한 번 포함해야 합니다.';
  }
  if (Number.isNaN(defaultSeqLength) || defaultSeqLength < 1 || defaultSeqLength > MAX_SEQ_LENGTH) {
    return null; // defaultSeqLength 오류는 별도 필드에서 표시
  }
  return null;
}

export function patternUsesParentToken(pattern: string): boolean {
  TOKEN_REGEX.lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = TOKEN_REGEX.exec(pattern)) !== null) {
    if (match[1] === 'PARENT') return true;
  }
  return false;
}

export const codeRuleValidators: ValidatorMap<CodeRuleFormValues> = {
  prefix: (v) => {
    if (v.length > 50) return '접두사는 50자 이하여야 합니다.';
    return null;
  },
  pattern: (v, all) => validatePattern(v, Number(all.defaultSeqLength)),
  defaultSeqLength: (v) => {
    if (v.trim() === '') return '기본 자릿수를 입력해주세요.';
    const n = Number(v);
    if (!Number.isInteger(n) || n < 1 || n > MAX_SEQ_LENGTH) {
      return `1~${MAX_SEQ_LENGTH} 사이의 정수여야 합니다.`;
    }
    return null;
  },
  description: (v) => {
    if (v.length > 500) return '메모는 500자 이하여야 합니다.';
    return null;
  },
};
