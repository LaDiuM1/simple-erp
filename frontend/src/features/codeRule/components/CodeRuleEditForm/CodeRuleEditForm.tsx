import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import Slider from '@mui/material/Slider';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useGetCodeRuleQuery } from '@/features/codeRule/api/codeRuleApi';
import {
  extractSeqLen,
  PRESET_BY_ID,
} from '@/features/codeRule/config/presets';
import { useCodeRuleEditForm } from '@/features/codeRule/hooks/useCodeRuleEditForm';
import {
  CODE_RULE_TARGET_LABEL,
  INPUT_MODE,
  INPUT_MODE_LABEL,
  RESET_POLICY,
  RESET_POLICY_LABEL,
  type CodeRule,
  type CodeRuleTarget,
} from '@/features/codeRule/types';
import PatternBuilder from '@/features/codeRule/components/PatternBuilder/PatternBuilder';
import PresetCards from '@/features/codeRule/components/PresetCards/PresetCards';
import PreviewPanel from '@/features/codeRule/components/PreviewPanel/PreviewPanel';
import { getErrorMessage } from '@/shared/api/error';
import {
  AdvancedSection,
  AdvancedTitle,
  Field,
  FieldHint,
  FieldLabel,
  FieldsColumn,
  FormGrid,
  FormRoot,
  InlineEditorRow,
  PreviewColumn,
  SectionTitle,
  SliderRow,
  SliderValueChip,
  TargetReadout,
} from './CodeRuleEditForm.styles';

const FORM_ID = 'code-rule-edit-form';
const SEQ_LEN_MIN = 3;
const SEQ_LEN_MAX = 6;
const MONOSPACE = 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';

interface CodeRuleEditFormProps {
  target: CodeRuleTarget;
}

/**
 * 데이터 fetch + 로딩/에러 분기는 outer 가, 폼 상태와 렌더는 Body 가.
 * Employee/Department EditForm 의 outer + Body 분해 패턴을 따른다.
 */
export default function CodeRuleEditForm({ target }: CodeRuleEditFormProps) {
  const { data, isLoading, isError, error, refetch } = useGetCodeRuleQuery(target);

  if (isLoading) return <LoadingScreen />;
  if (isError) {
    return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  }
  if (!data) return null;

  return <CodeRuleEditFormBody target={target} rule={data} />;
}

function CodeRuleEditFormBody({ target, rule }: { target: CodeRuleTarget; rule: CodeRule }) {
  const form = useCodeRuleEditForm(target, rule);
  const { values, update, validation, selectedPreset } = form;
  const preset = PRESET_BY_ID[selectedPreset];
  const isCustom = selectedPreset === 'custom';

  // 슬라이더가 표시할 자릿수 — 패턴의 {SEQ:n} 또는 defaultSeqLength
  const currentSeqLen =
    (extractSeqLen(values.pattern) ?? Number(values.defaultSeqLength)) || SEQ_LEN_MIN;

  // 단순 체크박스 — AUTO_OR_MANUAL 면 켜진 상태, 아니면 꺼진 상태
  const allowManualInput = values.inputMode === INPUT_MODE.AUTO_OR_MANUAL;

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'save',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.CODE_RULES,
          },
        ]}
      />

      <FormRoot>
        <FormGrid id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <FieldsColumn>
            {/* 코드 규칙 적용 대상 */}
            <TargetReadout>
              <span className="label">코드 규칙 적용 대상</span>
              <span className="value">{CODE_RULE_TARGET_LABEL[target]}</span>
            </TargetReadout>

            {/* 1. 코드 형식 카드 */}
            <Field>
              <SectionTitle>코드 형식</SectionTitle>
              <PresetCards selectedId={selectedPreset} onSelect={form.selectPreset} />
            </Field>

            {/* 2. 인라인 편집 (preset 모드) */}
            {!isCustom && (preset.hasPrefix || preset.hasSeqLen) && (
              <InlineEditorRow>
                {preset.hasPrefix ? (
                  <Field>
                    <FieldLabel>앞에 붙일 글자</FieldLabel>
                    <TextField
                      variant="outlined"
                      value={values.prefix}
                      onChange={(e) => form.setPrefix(e.target.value)}
                      onBlur={validation.onBlur('prefix')}
                      error={validation.isInvalid('prefix')}
                      helperText={validation.errorMessage('prefix') ?? '예: D, EMP, INV (생략 가능)'}
                      size="small"
                      fullWidth
                      slotProps={{
                        input: { sx: { fontFamily: MONOSPACE, fontSize: '0.875rem' } },
                        htmlInput: { maxLength: 50 },
                      }}
                    />
                  </Field>
                ) : (
                  <div />
                )}
                {preset.hasSeqLen && (
                  <Field>
                    <FieldLabel>순번 자릿수</FieldLabel>
                    <SliderRow>
                      <Slider
                        size="small"
                        value={currentSeqLen}
                        min={SEQ_LEN_MIN}
                        max={SEQ_LEN_MAX}
                        step={1}
                        marks
                        onChange={(_, v) => form.setSeqLen(Array.isArray(v) ? v[0] : v)}
                      />
                      <SliderValueChip>{currentSeqLen} 자리</SliderValueChip>
                    </SliderRow>
                    <FieldHint>
                      예: {'1'.padStart(currentSeqLen, '0')} ~ {'9'.repeat(currentSeqLen)}
                    </FieldHint>
                  </Field>
                )}
              </InlineEditorRow>
            )}

            {/* 3. 사용자 직접 입력 허용 (preset 모드 전용 단순 토글) */}
            {!isCustom && (
              <Field>
                <FieldLabel>입력 옵션</FieldLabel>
                <FormControlLabel
                  control={
                    <Checkbox
                      size="small"
                      checked={allowManualInput}
                      onChange={(e) =>
                        update('inputMode', e.target.checked ? INPUT_MODE.AUTO_OR_MANUAL : INPUT_MODE.AUTO)
                      }
                    />
                  }
                  label={
                    <Typography sx={{ fontSize: '0.875rem' }}>
                      수동 코드 입력 허용
                    </Typography>
                  }
                />
                <FieldHint>
                  체크하지 않으면 시스템이 항상 자동으로 코드를 부여합니다.
                </FieldHint>
              </Field>
            )}

            {/* 4. 메모 */}
            <Field>
              <FieldLabel>메모</FieldLabel>
              <TextField
                variant="outlined"
                value={values.description}
                onChange={(e) => update('description', e.target.value)}
                onBlur={validation.onBlur('description')}
                error={validation.isInvalid('description')}
                helperText={validation.errorMessage('description')}
                size="small"
                multiline
                minRows={2}
                fullWidth
              />
            </Field>

            {/* 5. 고급 영역 — custom 프리셋 선택 시만 */}
            {isCustom && (
              <AdvancedSection>
                <AdvancedTitle>고급 — 직접 만들기</AdvancedTitle>

                <Field>
                  <FieldLabel>접두사</FieldLabel>
                  <TextField
                    variant="outlined"
                    value={values.prefix}
                    onChange={(e) => form.setPrefix(e.target.value)}
                    onBlur={validation.onBlur('prefix')}
                    error={validation.isInvalid('prefix')}
                    helperText={
                      validation.errorMessage('prefix')
                      ?? '{PREFIX} 토큰 치환값. 예: D, EMP, INV (생략 가능)'
                    }
                    size="small"
                    fullWidth
                    slotProps={{
                      input: { sx: { fontFamily: MONOSPACE, fontSize: '0.875rem' } },
                    }}
                  />
                </Field>

                <Field>
                  <FieldLabel>패턴</FieldLabel>
                  <PatternBuilder
                    value={values.pattern}
                    onChange={(v) => update('pattern', v)}
                    onBlur={validation.onBlur('pattern')}
                    error={validation.isInvalid('pattern')}
                    helperText={validation.errorMessage('pattern')}
                  />
                </Field>

                <Field>
                  <FieldLabel>기본 순번 자릿수</FieldLabel>
                  <TextField
                    variant="outlined"
                    value={values.defaultSeqLength}
                    onChange={(e) => update('defaultSeqLength', e.target.value)}
                    onBlur={validation.onBlur('defaultSeqLength')}
                    error={validation.isInvalid('defaultSeqLength')}
                    helperText={
                      validation.errorMessage('defaultSeqLength')
                      ?? '{SEQ} 사용 시 적용. 1~18 사이'
                    }
                    size="small"
                    type="number"
                    fullWidth
                  />
                </Field>

                <Field>
                  <FieldLabel>입력 방식</FieldLabel>
                  <RadioGroup
                    row
                    value={values.inputMode}
                    onChange={(e) => update('inputMode', e.target.value as typeof values.inputMode)}
                  >
                    {(Object.keys(INPUT_MODE) as Array<keyof typeof INPUT_MODE>).map((k) => (
                      <FormControlLabel
                        key={k}
                        value={INPUT_MODE[k]}
                        control={<Radio size="small" />}
                        label={INPUT_MODE_LABEL[INPUT_MODE[k]]}
                        sx={{ '.MuiFormControlLabel-label': { fontSize: '0.875rem' } }}
                      />
                    ))}
                  </RadioGroup>
                </Field>

                <Field>
                  <FieldLabel>시퀀스 초기화 주기</FieldLabel>
                  <RadioGroup
                    row
                    value={values.resetPolicy}
                    onChange={(e) => update('resetPolicy', e.target.value as typeof values.resetPolicy)}
                  >
                    {(Object.keys(RESET_POLICY) as Array<keyof typeof RESET_POLICY>).map((k) => (
                      <FormControlLabel
                        key={k}
                        value={RESET_POLICY[k]}
                        control={<Radio size="small" />}
                        label={RESET_POLICY_LABEL[RESET_POLICY[k]]}
                        sx={{ '.MuiFormControlLabel-label': { fontSize: '0.875rem' } }}
                      />
                    ))}
                  </RadioGroup>
                </Field>

                <Field>
                  <FieldLabel>부모 시퀀스 분리</FieldLabel>
                  <FormControlLabel
                    control={
                      <Checkbox
                        size="small"
                        checked={values.parentScoped}
                        onChange={(e) => update('parentScoped', e.target.checked)}
                      />
                    }
                    label={
                      <Typography sx={{ fontSize: '0.875rem' }}>
                        부모별로 시퀀스 분리
                      </Typography>
                    }
                  />
                  <FieldHint>
                    체크 시 같은 부모 코드 안에서만 1, 2, 3, ... 으로 매겨집니다.
                    {form.patternUsesParent && ' {PARENT} 토큰을 사용 중이라면 함께 켜는 것을 권장.'}
                  </FieldHint>
                </Field>
              </AdvancedSection>
            )}
          </FieldsColumn>

          <PreviewColumn>
            {form.needsParentInput && (
              <Field>
                <FieldLabel>미리보기용 부모 코드</FieldLabel>
                <TextField
                  variant="outlined"
                  value={values.previewParentCode}
                  onChange={(e) => update('previewParentCode', e.target.value)}
                  size="small"
                  placeholder="예: D001"
                  fullWidth
                  helperText="저장 값에는 영향이 없으며, 미리보기 시뮬레이션에만 사용됩니다."
                  slotProps={{
                    input: { sx: { fontFamily: MONOSPACE, fontSize: '0.875rem' } },
                  }}
                />
              </Field>
            )}
            <PreviewPanel
              summary={form.summary}
              preview={form.preview}
              isLoading={form.isPreviewing}
              errorMessage={form.previewError}
              needsParentInput={form.needsParentInput}
              parentCode={values.previewParentCode}
            />
          </PreviewColumn>
        </FormGrid>
      </FormRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="채번 규칙 저장"
        message={`${CODE_RULE_TARGET_LABEL[target]} 규칙을 저장하시겠습니까?\n저장 즉시 새 규칙으로 채번이 시작됩니다.`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
