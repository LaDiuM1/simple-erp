/**
 * 백엔드 io.github.ladium1.erp.coderule.api 의 enum / DTO 미러.
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
  pattern: string;
  inputMode: InputMode;
  /** 도메인이 부모 개념을 갖는지 — 토큰 만들기 UI 의 부모 토큰 노출 여부 */
  hasParent: boolean;
  description: string | null;
  /** PARENT / 분류 토큰 사용 시 null */
  nextCode: string | null;
}

export interface CodeRuleAttributeDescriptor {
  key: string;
  label: string;
  values: { value: string; label: string }[];
}

export interface CodeRuleAttributeMapping {
  attributeKey: string;
  sourceValue: string;
  codeValue: string;
}

export interface CodeRuleUpdateRequest {
  pattern: string;
  inputMode: InputMode;
  description: string | null;
  attributeMappings: CodeRuleAttributeMapping[] | null;
}

export interface CodeRulePreviewRequest {
  pattern: string;
  inputMode: InputMode;
  parentCode: string | null;
  previewAttributes: Record<string, string> | null;
  attributeMappings: CodeRuleAttributeMapping[] | null;
}

export interface CodeRulePreviewResponse {
  nextCode: string;
  samples: string[];
}

// Form values

export interface CodeRuleFormValues {
  pattern: string;
  inputMode: InputMode;
  description: string;
  /** 미리보기용 부모 코드 (저장에는 영향 없음) */
  previewParentCode: string;
  /** 편집 중인 분류 매핑 — 저장 시 BE 가 replace */
  attributeMappings: CodeRuleAttributeMapping[];
  /** 미리보기 시 사용할 attribute sourceValue (key -> value) */
  previewAttributes: Record<string, string>;
}

export function codeRuleToFormValues(
  rule: CodeRule,
  mappings: CodeRuleAttributeMapping[] = [],
): CodeRuleFormValues {
  return {
    pattern: rule.pattern,
    inputMode: rule.inputMode,
    description: rule.description ?? '',
    previewParentCode: '',
    attributeMappings: mappings,
    previewAttributes: {},
  };
}

/** 빈 codeValue (사용자가 미입력) 매핑은 BE 의 @NotBlank 검증에 걸리므로 사전 제거. */
function filledMappings(mappings: CodeRuleAttributeMapping[]): CodeRuleAttributeMapping[] {
  return mappings.filter(
    (m) => m.codeValue !== null && m.codeValue !== undefined && m.codeValue.trim() !== '',
  );
}

export function codeRuleFormToUpdateRequest(v: CodeRuleFormValues): CodeRuleUpdateRequest {
  const trimmedDescription = v.description.trim();
  return {
    pattern: v.pattern.trim(),
    inputMode: v.inputMode,
    description: trimmedDescription === '' ? null : trimmedDescription,
    attributeMappings: filledMappings(v.attributeMappings),
  };
}

export function codeRuleFormToPreviewRequest(v: CodeRuleFormValues): CodeRulePreviewRequest {
  const trimmedParent = v.previewParentCode.trim();
  return {
    pattern: v.pattern,
    inputMode: v.inputMode,
    parentCode: trimmedParent === '' ? null : trimmedParent,
    previewAttributes: v.previewAttributes,
    attributeMappings: filledMappings(v.attributeMappings),
  };
}
