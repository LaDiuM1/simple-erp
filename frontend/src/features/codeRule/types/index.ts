/**
 * 백엔드 io.github.ladium1.erp.global.coderule.api 의 enum / DTO 미러.
 * BE 변경 시 함께 갱신.
 */

export const CODE_RULE_TARGET = {
  DEPARTMENT: 'DEPARTMENT',
  POSITION: 'POSITION',
  CUSTOMER: 'CUSTOMER',
} as const;

export type CodeRuleTarget = (typeof CODE_RULE_TARGET)[keyof typeof CODE_RULE_TARGET];

export const CODE_RULE_TARGET_LABEL: Record<CodeRuleTarget, string> = {
  DEPARTMENT: '부서 코드',
  POSITION: '직책 코드',
  CUSTOMER: '고객사 코드',
};

export const RESET_POLICY = {
  NEVER: 'NEVER',
  YEARLY: 'YEARLY',
  MONTHLY: 'MONTHLY',
  DAILY: 'DAILY',
} as const;

export type ResetPolicy = (typeof RESET_POLICY)[keyof typeof RESET_POLICY];

export const RESET_POLICY_LABEL: Record<ResetPolicy, string> = {
  NEVER: '초기화 없음',
  YEARLY: '매년',
  MONTHLY: '매월',
  DAILY: '매일',
};

export const INPUT_MODE = {
  AUTO: 'AUTO',
  MANUAL: 'MANUAL',
  AUTO_OR_MANUAL: 'AUTO_OR_MANUAL',
} as const;

export type InputMode = (typeof INPUT_MODE)[keyof typeof INPUT_MODE];

export const INPUT_MODE_LABEL: Record<InputMode, string> = {
  AUTO: '자동',
  MANUAL: '수동',
  AUTO_OR_MANUAL: '선택 (자동/수동)',
};

// Server DTO 미러

export interface CodeRule {
  id: number;
  target: CodeRuleTarget;
  targetLabel: string;
  prefix: string | null;
  pattern: string;
  defaultSeqLength: number;
  resetPolicy: ResetPolicy;
  inputMode: InputMode;
  parentScoped: boolean;
  description: string | null;
  /** {PARENT} 토큰 사용 시 null (parent 알 수 없음) */
  nextCode: string | null;
}

export interface CodeRuleUpdateRequest {
  prefix: string | null;
  pattern: string;
  defaultSeqLength: number;
  resetPolicy: ResetPolicy;
  inputMode: InputMode;
  parentScoped: boolean;
  description: string | null;
}

export interface CodeRulePreviewRequest {
  prefix: string | null;
  pattern: string;
  defaultSeqLength: number;
  resetPolicy: ResetPolicy;
  inputMode: InputMode;
  parentScoped: boolean;
  parentCode: string | null;
}

export interface CodeRulePreviewResponse {
  nextCode: string;
  samples: string[];
}

// Form values — MUI 호환 위해 숫자도 string 보관, 서버 전송 시 변환

export interface CodeRuleFormValues {
  prefix: string;
  pattern: string;
  defaultSeqLength: string;
  resetPolicy: ResetPolicy;
  inputMode: InputMode;
  parentScoped: boolean;
  description: string;
  /** 미리보기용 부모 코드 (저장에는 영향 없음) */
  previewParentCode: string;
}

export function codeRuleToFormValues(rule: CodeRule): CodeRuleFormValues {
  return {
    prefix: rule.prefix ?? '',
    pattern: rule.pattern,
    defaultSeqLength: String(rule.defaultSeqLength),
    resetPolicy: rule.resetPolicy,
    inputMode: rule.inputMode,
    parentScoped: rule.parentScoped,
    description: rule.description ?? '',
    previewParentCode: '',
  };
}

export function codeRuleFormToUpdateRequest(v: CodeRuleFormValues): CodeRuleUpdateRequest {
  const trimmedPrefix = v.prefix.trim();
  const trimmedDescription = v.description.trim();
  return {
    prefix: trimmedPrefix === '' ? null : trimmedPrefix,
    pattern: v.pattern.trim(),
    defaultSeqLength: Number(v.defaultSeqLength),
    resetPolicy: v.resetPolicy,
    inputMode: v.inputMode,
    parentScoped: v.parentScoped,
    description: trimmedDescription === '' ? null : trimmedDescription,
  };
}

export function codeRuleFormToPreviewRequest(v: CodeRuleFormValues): CodeRulePreviewRequest {
  const trimmedPrefix = v.prefix.trim();
  const trimmedParent = v.previewParentCode.trim();
  return {
    prefix: trimmedPrefix === '' ? null : trimmedPrefix,
    pattern: v.pattern,
    defaultSeqLength: Number(v.defaultSeqLength) || 1,
    resetPolicy: v.resetPolicy,
    inputMode: v.inputMode,
    parentScoped: v.parentScoped,
    parentCode: trimmedParent === '' ? null : trimmedParent,
  };
}
