import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import { INPUT_MODE, type CodeRuleFormValues } from '@/features/codeRule/types';

const TOKEN_REGEX = /\{([A-Z]+)(?::(\d+))?}/g;
const BUILT_IN_TOKENS = new Set(['YYYY', 'YY', 'MM', 'DD', 'SEQ', 'PARENT']);
const MAX_SEQ_LENGTH = 18;

/**
 * 클라이언트 사전 검증 — BE PatternCompiler.validate 미러.
 *
 * @param attributeKeys 도메인이 등록한 attribute key (예: TYPE) — 이들은 unknown 으로 거부 안 함.
 */
export function validatePattern(pattern: string, attributeKeys: Set<string> = new Set()): string | null {
  const trimmed = pattern.trim();
  if (trimmed === '') return '패턴을 입력해주세요.';
  if (trimmed.length > 200) return '패턴은 200자 이하여야 합니다.';

  let hasSeq = false;
  TOKEN_REGEX.lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = TOKEN_REGEX.exec(trimmed)) !== null) {
    const [, name, arg] = match;
    const isBuiltIn = BUILT_IN_TOKENS.has(name);
    const isAttribute = attributeKeys.has(name);
    if (!isBuiltIn && !isAttribute) {
      return `알 수 없는 토큰: {${name}}`;
    }
    if (arg !== undefined && name !== 'SEQ') {
      return `{${name}} 토큰은 자릿수 인자를 가질 수 없습니다.`;
    }
    if (name === 'SEQ') {
      hasSeq = true;
      if (arg === undefined) {
        return '{SEQ:n} 형식으로 자릿수를 명시해주세요.';
      }
      const n = Number(arg);
      if (n < 1 || n > MAX_SEQ_LENGTH) {
        return `SEQ 자릿수는 1~${MAX_SEQ_LENGTH} 사이여야 합니다.`;
      }
    }
  }
  if (!hasSeq) {
    return '{SEQ:n} 토큰을 최소 한 번 포함해야 합니다.';
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
  // MANUAL 모드는 패턴이 의미 없어 검증 skip — 사용자가 자동에서 패턴 오류 상태로 수동으로 이동해도 저장 가능
  pattern: (v, all) => (all.inputMode === INPUT_MODE.MANUAL ? null : validatePattern(v)),
  description: (v) => {
    if (v.length > 500) return '메모는 500자 이하여야 합니다.';
    return null;
  },
};
