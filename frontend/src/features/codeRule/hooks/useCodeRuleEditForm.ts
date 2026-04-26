import * as React from 'react';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { useFieldValidation, type FieldValidation } from '@/shared/hooks/useFieldValidation';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  usePreviewCodeRuleMutation,
  useUpdateCodeRuleMutation,
} from '@/features/codeRule/api/codeRuleApi';
import {
  detectPreset,
  extractSeqLen,
  PRESET_BY_ID,
  summarize,
  type PresetId,
} from '@/features/codeRule/config/presets';
import {
  codeRuleFormToPreviewRequest,
  codeRuleFormToUpdateRequest,
  codeRuleToFormValues,
  type CodeRule,
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
  /** 현재 선택된 프리셋 — UI 분기용 (서버 전송 X). */
  selectedPreset: PresetId;
  /** 카드 클릭 시 호출 — pattern/resetPolicy/parentScoped 일괄 갱신. */
  selectPreset: (id: PresetId) => void;
  /** 인라인 prefix 변경. preset 모드면 pattern 까지 재빌드. */
  setPrefix: (prefix: string) => void;
  /** 인라인 자릿수 변경. preset 모드면 pattern 까지 재빌드. */
  setSeqLen: (seqLen: number) => void;
  /** 사람말 요약 — 미리보기 패널에 표시. */
  summary: string;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

/**
 * rule 이 이미 로드된 시점에 호출. 로딩/에러 분기는 호출자가 처리.
 * Employee 의 useEmployeeEditForm 과 동일한 시그니처 패턴 (id, detail) → (target, rule).
 */
export function useCodeRuleEditForm(
  target: CodeRuleTarget,
  rule: CodeRule,
): CodeRuleEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();

  const [values, setValues] = useState<CodeRuleFormValues>(() => codeRuleToFormValues(rule));
  const [selectedPreset, setSelectedPreset] = useState<PresetId>(() =>
    detectPreset({
      pattern: rule.pattern,
      resetPolicy: rule.resetPolicy,
      parentScoped: rule.parentScoped,
    }),
  );
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [preview, setPreview] = useState<CodeRulePreviewResponse | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);

  const [updateCodeRule, { isLoading: isSaving }] = useUpdateCodeRuleMutation();
  const [previewCodeRule, { isLoading: isPreviewing }] = usePreviewCodeRuleMutation();

  const update = <K extends keyof CodeRuleFormValues>(key: K, v: CodeRuleFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const validation = useFieldValidation(values, codeRuleValidators);

  const patternUsesParent = useMemo(
    () => patternUsesParentToken(values.pattern),
    [values.pattern],
  );
  const needsParentInput = patternUsesParent || values.parentScoped;

  /* ---- 프리셋 변경 시 pattern/resetPolicy/parentScoped 일괄 갱신 ---- */
  const selectPreset = (id: PresetId) => {
    setSelectedPreset(id);
    if (id === 'custom') {
      // custom 으로 전환 시 현재 pattern 그대로 유지하고 모든 옵션 자유 편집 허용
      return;
    }
    const seqLen = (extractSeqLen(values.pattern) ?? Number(values.defaultSeqLength)) || 3;
    const built = PRESET_BY_ID[id].build(values.prefix, seqLen);
    setValues((prev) => ({
      ...prev,
      pattern: built.pattern,
      resetPolicy: built.resetPolicy,
      parentScoped: built.parentScoped,
      defaultSeqLength: String(seqLen),
    }));
  };

  /* ---- 인라인 prefix/seqLen 편집 — preset 모드일 때만 pattern 재빌드 ---- */
  const setPrefix = (prefix: string) => {
    setValues((prev) => {
      if (selectedPreset === 'custom') {
        return { ...prev, prefix };
      }
      const seqLen = (extractSeqLen(prev.pattern) ?? Number(prev.defaultSeqLength)) || 3;
      const built = PRESET_BY_ID[selectedPreset].build(prefix, seqLen);
      return { ...prev, prefix, pattern: built.pattern };
    });
  };

  const setSeqLen = (seqLen: number) => {
    setValues((prev) => {
      if (selectedPreset === 'custom') {
        return { ...prev, defaultSeqLength: String(seqLen) };
      }
      const built = PRESET_BY_ID[selectedPreset].build(prev.prefix, seqLen);
      return {
        ...prev,
        pattern: built.pattern,
        defaultSeqLength: String(seqLen),
      };
    });
  };

  /* ---- 사람말 요약 (미리보기 패널 헤드라인) ---- */
  const summary = useMemo(
    () => summarize({
      preset: selectedPreset,
      prefix: values.prefix,
      seqLen: (extractSeqLen(values.pattern) ?? Number(values.defaultSeqLength)) || 3,
      pattern: values.pattern,
      inputMode: values.inputMode,
    }),
    [selectedPreset, values.prefix, values.pattern, values.defaultSeqLength, values.inputMode],
  );

  // 미리보기 — 패턴/접두사/시퀀스/정책/parent 변경 시 debounced 호출
  const debounced = useDebouncedValue(values, 350);

  useEffect(() => {
    const patternError = validatePattern(debounced.pattern, Number(debounced.defaultSeqLength));
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
  }, [debounced, previewCodeRule, target]);

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
      await updateCodeRule({ target, body: codeRuleFormToUpdateRequest(values) }).unwrap();
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
    selectedPreset,
    selectPreset,
    setPrefix,
    setSeqLen,
    summary,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: () => setConfirmOpen(false),
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.CODE_RULES]),
  };
}
