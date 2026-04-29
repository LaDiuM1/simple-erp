import * as React from 'react';
import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { useFieldValidation, type FieldValidation } from '@/shared/hooks/useFieldValidation';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useGetCodeRuleAttributeMappingsQuery,
  useGetCodeRuleAttributesQuery,
  usePreviewCodeRuleMutation,
  useUpdateCodeRuleMutation,
} from '@/features/codeRule/api/codeRuleApi';
import {
  codeRuleFormToPreviewRequest,
  codeRuleFormToUpdateRequest,
  codeRuleToFormValues,
  INPUT_MODE,
  type CodeRule,
  type CodeRuleAttributeDescriptor,
  type CodeRuleAttributeMapping,
  type CodeRuleFormValues,
  type CodeRulePreviewResponse,
  type CodeRuleTarget,
} from '@/features/codeRule/types';
import {
  codeRuleValidators,
  patternUsesParentToken,
  validatePattern,
} from '@/features/codeRule/validation/codeRuleValidation';
import { getErrorMessage } from '@/shared/api/error';

export interface CodeRuleEditFormState {
  values: CodeRuleFormValues;
  update: <K extends keyof CodeRuleFormValues>(key: K, v: CodeRuleFormValues[K]) => void;
  validation: FieldValidation<CodeRuleFormValues>;
  isSaving: boolean;
  confirmOpen: boolean;
  preview: CodeRulePreviewResponse | null;
  isPreviewing: boolean;
  previewError: string | null;
  patternUsesParent: boolean;
  needsParentInput: boolean;
  showAutoOptions: boolean;
  patternInputRef: React.RefObject<HTMLInputElement | null>;
  /** 도메인 attribute key 집합 — TokenChipsCard / 검증 용 */
  attributeKeySet: Set<string>;
  tokenModalOpen: boolean;
  openTokenModal: () => void;
  closeTokenModal: () => void;
  /** 사용자 입력값 literal 을 토큰 카드에 추가 */
  addCustomLiteral: (literal: string) => void;
  removeCustomLiteral: (literal: string) => void;
  customLiterals: string[];
  /** 토큰 카드 chip 클릭 시 — 패턴의 cursor 위치에 토큰 삽입 */
  insertTokenAtCursor: (token: string) => void;
  /** 분류값 종류로 진입 시 — 두번째 모달 호출 */
  attributeDialogOpen: boolean;
  openAttributeDialog: () => void;
  closeAttributeDialog: () => void;
  /** 두번째 모달의 추가 — 매핑 1건 + 패턴에 {KEY} 토큰 보장 */
  onAttributeMappingConfirm: (mapping: CodeRuleAttributeMapping) => void;
  /** chip 의 매핑 entry 우측 X — 매핑 1건 제거 */
  removeMapping: (attributeKey: string, sourceValue: string) => void;
  attributes: CodeRuleAttributeDescriptor[];
  setPreviewAttribute: (attributeKey: string, sourceValue: string) => void;
  needsAttributeInput: boolean;
  usedAttributeKeys: string[];
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useCodeRuleEditForm(
  target: CodeRuleTarget,
  rule: CodeRule,
): CodeRuleEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();

  const { data: attributes = [] } = useGetCodeRuleAttributesQuery(target);
  const { data: fetchedMappings } = useGetCodeRuleAttributeMappingsQuery(target);

  const [values, setValues] = useState<CodeRuleFormValues>(() => codeRuleToFormValues(rule));
  const [mappingsInitialized, setMappingsInitialized] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [preview, setPreview] = useState<CodeRulePreviewResponse | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [tokenModalOpen, setTokenModalOpen] = useState(false);
  const [attributeDialogOpen, setAttributeDialogOpen] = useState(false);
  const [customLiterals, setCustomLiterals] = useState<string[]>([]);

  const patternInputRef = useRef<HTMLInputElement | null>(null);

  const [updateCodeRule, { isLoading: isSaving }] = useUpdateCodeRuleMutation();
  const [previewCodeRule, { isLoading: isPreviewing }] = usePreviewCodeRuleMutation();

  // 매핑 fetch 완료 시 form values 에 1회 주입 (raw — 빈 row 자동 생성 X)
  useEffect(() => {
    if (mappingsInitialized || fetchedMappings === undefined) return;
    setValues((prev) => ({ ...prev, attributeMappings: fetchedMappings }));
    setMappingsInitialized(true);
  }, [fetchedMappings, mappingsInitialized]);

  const update = <K extends keyof CodeRuleFormValues>(key: K, v: CodeRuleFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const validation = useFieldValidation(values, codeRuleValidators);

  const showAutoOptions =
    values.inputMode === INPUT_MODE.AUTO || values.inputMode === INPUT_MODE.AUTO_OR_MANUAL;

  const patternUsesParent = useMemo(
    () => patternUsesParentToken(values.pattern),
    [values.pattern],
  );
  const needsParentInput = rule.hasParent && patternUsesParent;

  const attributeKeySet = useMemo(
    () => new Set(attributes.map((a) => a.key)),
    [attributes],
  );
  const usedAttributeKeys = useMemo(
    () => extractUsedAttributeKeys(values.pattern, attributeKeySet),
    [values.pattern, attributeKeySet],
  );
  const needsAttributeInput = usedAttributeKeys.length > 0;

  const openTokenModal = () => setTokenModalOpen(true);
  const closeTokenModal = () => setTokenModalOpen(false);

  const insertTokenAtCursor = (token: string) => {
    setValues((prev) => {
      const input = patternInputRef.current;
      const start = input?.selectionStart ?? prev.pattern.length;
      const end = input?.selectionEnd ?? prev.pattern.length;
      const nextPattern = prev.pattern.slice(0, start) + token + prev.pattern.slice(end);
      return { ...prev, pattern: nextPattern };
    });
    queueMicrotask(() => {
      const input = patternInputRef.current;
      if (!input) return;
      input.focus();
      const start = input.selectionStart ?? 0;
      input.setSelectionRange(start + token.length, start + token.length);
    });
  };

  const addCustomLiteral = (literal: string) => {
    const trimmed = literal.trim();
    if (!trimmed) return;
    setCustomLiterals((prev) => (prev.includes(trimmed) ? prev : [...prev, trimmed]));
    setTokenModalOpen(false);
  };

  const removeCustomLiteral = (literal: string) => {
    setCustomLiterals((prev) => prev.filter((l) => l !== literal));
  };

  const openAttributeDialog = () => setAttributeDialogOpen(true);
  const closeAttributeDialog = () => setAttributeDialogOpen(false);

  /** 두번째 모달의 추가 — 매핑 1건 추가/갱신 + 패턴에 {KEY} 가 없으면 끝에 추가 */
  const onAttributeMappingConfirm = (mapping: CodeRuleAttributeMapping) => {
    setValues((prev) => {
      const tokenLiteral = `{${mapping.attributeKey}}`;
      const hasToken = prev.pattern.includes(tokenLiteral);
      const nextPattern = hasToken ? prev.pattern : prev.pattern + tokenLiteral;

      const idx = prev.attributeMappings.findIndex(
        (m) => m.attributeKey === mapping.attributeKey && m.sourceValue === mapping.sourceValue,
      );
      const nextMappings = [...prev.attributeMappings];
      if (idx >= 0) {
        nextMappings[idx] = mapping;
      } else {
        nextMappings.push(mapping);
      }
      return { ...prev, pattern: nextPattern, attributeMappings: nextMappings };
    });
    setAttributeDialogOpen(false);
  };

  const removeMapping = (attributeKey: string, sourceValue: string) => {
    setValues((prev) => ({
      ...prev,
      attributeMappings: prev.attributeMappings.filter(
        (m) => !(m.attributeKey === attributeKey && m.sourceValue === sourceValue),
      ),
    }));
  };

  const setPreviewAttribute = (attributeKey: string, sourceValue: string) => {
    setValues((prev) => ({
      ...prev,
      previewAttributes: { ...prev.previewAttributes, [attributeKey]: sourceValue },
    }));
  };

  const debounced = useDebouncedValue(values, 350);

  useEffect(() => {
    if (debounced.inputMode === INPUT_MODE.MANUAL) {
      setPreview(null);
      setPreviewError(null);
      return;
    }
    const patternError = validatePattern(debounced.pattern, attributeKeySet);
    if (patternError) {
      setPreview(null);
      setPreviewError(null);
      return;
    }
    let cancelled = false;
    previewCodeRule({ target, body: codeRuleFormToPreviewRequest(debounced) })
      .unwrap()
      .then((res) => {
        if (!cancelled) {
          setPreview(res);
          setPreviewError(null);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setPreview(null);
          setPreviewError(getErrorMessage(err, '미리보기 조회 실패'));
        }
      });
    return () => {
      cancelled = true;
    };
  }, [debounced, previewCodeRule, target, attributeKeySet]);

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    setConfirmOpen(true);
  };

  const handleConfirmedSubmit = async () => {
    setConfirmOpen(false);
    try {
      await updateCodeRule({
        target,
        body: codeRuleFormToUpdateRequest(values),
      }).unwrap();
      snackbar.success('저장되었습니다.');
      navigate(MENU_PATH[MENU_CODE.CODE_RULES]);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '저장 중 오류가 발생했습니다.'));
    }
  };

  return {
    values,
    update,
    validation,
    isSaving,
    confirmOpen,
    preview,
    isPreviewing,
    previewError,
    patternUsesParent,
    needsParentInput,
    showAutoOptions,
    patternInputRef,
    attributeKeySet,
    tokenModalOpen,
    openTokenModal,
    closeTokenModal,
    addCustomLiteral,
    removeCustomLiteral,
    customLiterals,
    insertTokenAtCursor,
    attributeDialogOpen,
    openAttributeDialog,
    closeAttributeDialog,
    onAttributeMappingConfirm,
    removeMapping,
    attributes,
    setPreviewAttribute,
    needsAttributeInput,
    usedAttributeKeys,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: () => setConfirmOpen(false),
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.CODE_RULES]),
  };
}

const TOKEN_REGEX = /\{([A-Z]+)(?::\d+)?}/g;

function extractUsedAttributeKeys(pattern: string, attributeKeys: Set<string>): string[] {
  const used: string[] = [];
  TOKEN_REGEX.lastIndex = 0;
  let match: RegExpExecArray | null;
  while ((match = TOKEN_REGEX.exec(pattern)) !== null) {
    const name = match[1];
    if (attributeKeys.has(name) && !used.includes(name)) {
      used.push(name);
    }
  }
  return used;
}
