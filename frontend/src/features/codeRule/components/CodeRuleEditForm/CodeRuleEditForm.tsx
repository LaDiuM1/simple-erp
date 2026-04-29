import Radio from '@mui/material/Radio';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import MenuItem from '@mui/material/MenuItem';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import PageHeaderTitle from '@/shared/ui/layout/PageHeaderTitle';
import { useGetCodeRuleQuery } from '@/features/codeRule/api/codeRuleApi';
import { useCodeRuleEditForm } from '@/features/codeRule/hooks/useCodeRuleEditForm';
import {
  CODE_RULE_TARGET_LABEL,
  INPUT_MODE,
  INPUT_MODE_LABEL,
  type CodeRule,
  type CodeRuleTarget,
} from '@/features/codeRule/types';
import AttributeMappingDialog from '@/features/codeRule/components/AttributeMappingDialog/AttributeMappingDialog';
import PatternBuilder from '@/features/codeRule/components/PatternBuilder/PatternBuilder';
import PreviewPanel from '@/features/codeRule/components/PreviewPanel/PreviewPanel';
import TokenBuilderModal from '@/features/codeRule/components/TokenBuilderModal/TokenBuilderModal';
import TokenChipsCard from '@/features/codeRule/components/TokenChipsCard/TokenChipsCard';
import { getErrorMessage } from '@/shared/api/error';
import {
  Field,
  FieldLabel,
  FieldsColumn,
  FormGrid,
  FormRoot,
  InputModeChoice,
  InputModeChoiceLabel,
  InputModeChoices,
  InputModeHintBox,
  PreviewColumn,
  PreviewSticky,
  SectionTitle,
} from './CodeRuleEditForm.styles';

const FORM_ID = 'code-rule-edit-form';
const MONOSPACE = 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';

const INPUT_MODE_HINTS: Record<keyof typeof INPUT_MODE, string> = {
  AUTO: '시스템이 자동으로 코드를 생성합니다. 사용자는 코드를 직접 입력할 수 없습니다.',
  MANUAL: '사용자가 코드를 직접 입력합니다. 자동 생성 / 패턴은 적용되지 않습니다.',
  AUTO_OR_MANUAL: '기본은 자동 생성, 필요 시 사용자가 직접 입력할 수 있습니다.',
};

interface Props {
  target: CodeRuleTarget;
}

export default function CodeRuleEditForm({ target }: Props) {
  const { data, isLoading, isError, error, refetch } = useGetCodeRuleQuery(target);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  if (!data) return null;

  return <Body target={target} rule={data} />;
}

function Body({ target, rule }: { target: CodeRuleTarget; rule: CodeRule }) {
  const form = useCodeRuleEditForm(target, rule);
  const { values, update, validation } = form;

  const targetLabel = CODE_RULE_TARGET_LABEL[target];
  const inputModeKey = (Object.keys(INPUT_MODE) as Array<keyof typeof INPUT_MODE>)
    .find((k) => INPUT_MODE[k] === values.inputMode) ?? 'AUTO';

  return (
    <>
      <PageHeaderTitle>{`${targetLabel} 수정`}</PageHeaderTitle>
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
            {/* 1. 입력 방식 — 가로 3분할 */}
            <Field>
              <SectionTitle>입력 방식</SectionTitle>
              <InputModeChoices>
                {(Object.keys(INPUT_MODE) as Array<keyof typeof INPUT_MODE>).map((k) => {
                  const value = INPUT_MODE[k];
                  const selected = values.inputMode === value;
                  return (
                    <InputModeChoice
                      key={k}
                      data-selected={selected}
                      onClick={() => update('inputMode', value)}
                      role="radio"
                      aria-checked={selected}
                    >
                      <Typography sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                        <Radio size="small" checked={selected} sx={{ p: 0 }} />
                        <InputModeChoiceLabel>{INPUT_MODE_LABEL[value]}</InputModeChoiceLabel>
                      </Typography>
                    </InputModeChoice>
                  );
                })}
              </InputModeChoices>
            </Field>

            {/* 2. 자동 채번 옵션 — AUTO / AUTO_OR_MANUAL 일 때만 */}
            {form.showAutoOptions && (
              <>
                <Field>
                  <SectionTitle>패턴</SectionTitle>
                  <PatternBuilder
                    ref={form.patternInputRef}
                    value={values.pattern}
                    onChange={(v) => update('pattern', v)}
                    onBlur={validation.onBlur('pattern')}
                    error={validation.isInvalid('pattern')}
                    helperText={validation.errorMessage('pattern')}
                  />
                </Field>

                <TokenChipsCard
                  hasParent={rule.hasParent}
                  attributes={form.attributes}
                  customLiterals={form.customLiterals}
                  mappings={values.attributeMappings}
                  onSelectToken={form.insertTokenAtCursor}
                  onRemoveLiteral={form.removeCustomLiteral}
                  onRemoveMapping={form.removeMapping}
                  onOpenTokenBuilder={form.openTokenModal}
                />
              </>
            )}

            {/* 메모 — 좌측 컬럼 맨 아래 */}
            <Field>
              <SectionTitle>메모</SectionTitle>
              <TextField
                variant="outlined"
                value={values.description}
                onChange={(e) => update('description', e.target.value)}
                onBlur={validation.onBlur('description')}
                error={validation.isInvalid('description')}
                helperText={validation.errorMessage('description')}
                size="small"
                multiline
                minRows={3}
                fullWidth
                placeholder="규칙에 대한 메모 (선택)"
              />
            </Field>
          </FieldsColumn>

          <PreviewColumn>
            <PreviewSticky>
              <InputModeHintBox>
                <Typography sx={{ fontSize: '0.75rem', fontWeight: 700, color: 'text.primary', mb: 0.5 }}>
                  {INPUT_MODE_LABEL[values.inputMode]}
                </Typography>
                {INPUT_MODE_HINTS[inputModeKey]}
              </InputModeHintBox>

              {form.showAutoOptions && (
                <>
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
                        helperText="저장 값에는 영향 없음. 미리보기 시뮬레이션 용."
                        slotProps={{
                          input: { sx: { fontFamily: MONOSPACE, fontSize: '0.875rem' } },
                        }}
                      />
                    </Field>
                  )}
                  {form.needsAttributeInput && form.usedAttributeKeys.map((key) => {
                    const desc = form.attributes.find((a) => a.key === key);
                    if (!desc) return null;
                    return (
                      <Field key={key}>
                        <FieldLabel>{`미리보기용 ${desc.label}`}</FieldLabel>
                        <TextField
                          select
                          variant="outlined"
                          size="small"
                          fullWidth
                          value={values.previewAttributes[key] ?? ''}
                          onChange={(e) => form.setPreviewAttribute(key, e.target.value)}
                          helperText="저장 값에는 영향 없음. 미리보기 시뮬레이션 용."
                        >
                          <MenuItem value="">선택 안 함</MenuItem>
                          {desc.values.map((v) => (
                            <MenuItem key={v.value} value={v.value}>
                              {v.label}
                            </MenuItem>
                          ))}
                        </TextField>
                      </Field>
                    );
                  })}
                  <PreviewPanel
                    preview={form.preview}
                    isLoading={form.isPreviewing}
                    errorMessage={form.previewError}
                    needsParentInput={form.needsParentInput}
                    parentCode={values.previewParentCode}
                  />
                </>
              )}
            </PreviewSticky>
          </PreviewColumn>
        </FormGrid>
      </FormRoot>

      <TokenBuilderModal
        open={form.tokenModalOpen}
        attributes={form.attributes}
        onClose={form.closeTokenModal}
        onAddLiteral={form.addCustomLiteral}
        onSelectAttribute={() => {
          form.closeTokenModal();
          form.openAttributeDialog();
        }}
      />

      <AttributeMappingDialog
        open={form.attributeDialogOpen}
        attributes={form.attributes}
        existingMappings={values.attributeMappings}
        onClose={form.closeAttributeDialog}
        onConfirm={form.onAttributeMappingConfirm}
      />

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="채번 규칙 저장"
        message={`${targetLabel} 규칙을 저장하시겠습니까?\n저장 즉시 새 규칙으로 채번이 시작됩니다.`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
