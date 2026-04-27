import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Theme } from '@mui/material/styles';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { usePermission } from '@/shared/hooks/usePermission';
import { getErrorMessage } from '@/shared/api/error';
import FormField from './FormField';
import FormSection from './FormSection';
import { FormGrid, FormRoot, FormSurface } from './GenericForm.styles';
import { useFormState } from './useFormState';
import type { FieldConfig, FormApiConfig, FormSectionInfo, FormState } from './types';

/** PageHeader 의 저장 버튼이 portal 을 넘어 form 을 연결할 때 쓰는 id. */
const FORM_ID = 'generic-form';

const DEFAULT_CREATE_SUCCESS = '등록되었습니다.';
const DEFAULT_EDIT_SUCCESS = '저장되었습니다.';
const DEFAULT_SAVE_ERROR = '저장 중 오류가 발생했습니다.';

export interface GenericFormProps<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
> {
  api: FormApiConfig<TValues, TDetail, TCreateRequest, TUpdateRequest>;
  fields: FieldConfig<TValues>[];
  /** 수정 / 상세 모드 대상 ID. 미지정 시 생성 모드. */
  id?: number;
  /** id 와 함께 지정 시 상세 모드 — 모든 필드 disabled, 헤더는 "목록으로" + "수정". */
  readOnly?: boolean;
}

/**
 * 도메인 등록/수정/상세 페이지 단일 규격 컴포넌트.
 * `id` 유무로 CreateForm / EditForm 분기, `readOnly` 면 DetailForm — 각 하위 컴포넌트가 고정된 훅 호출 순서를 가짐.
 */
export default function GenericForm<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
>({
  api,
  fields,
  id,
  readOnly,
}: GenericFormProps<TValues, TDetail, TCreateRequest, TUpdateRequest>) {
  if (id != null && readOnly) {
    return <DetailForm api={api} fields={fields} id={id} />;
  }
  if (id != null) {
    return <EditForm api={api} fields={fields} id={id} />;
  }
  return <CreateForm api={api} fields={fields} />;
}

function CreateForm<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
>({
  api,
  fields,
}: {
  api: FormApiConfig<TValues, TDetail, TCreateRequest, TUpdateRequest>;
  fields: FieldConfig<TValues>[];
}) {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const formState = useFormState<TValues>(api.emptyValues);
  const [createFn, { isLoading: isSaving }] = api.useCreate();
  const [confirmOpen, setConfirmOpen] = useState(false);

  const visibleFields = fields.filter((f) => !f.hideOnCreate);

  const doCreate = async () => {
    try {
      await createFn(api.toCreateRequest(formState.values)).unwrap();
      snackbar.success(api.successMessages?.create ?? DEFAULT_CREATE_SUCCESS);
      navigate(api.listPath);
    } catch (err) {
      snackbar.error(getErrorMessage(err, DEFAULT_SAVE_ERROR));
    }
  };

  const handleSubmit = async () => {
    if (api.confirm) {
      setConfirmOpen(true);
      return;
    }
    await doCreate();
  };

  return (
    <>
      <FormBody
        menuCode={api.menuCode}
        fields={visibleFields}
        formState={formState}
        isSaving={isSaving}
        mode="create"
        section={api.section}
        onSubmit={handleSubmit}
        onCancel={() => navigate(api.listPath)}
      />
      {api.confirm && (
        <ConfirmModal
          isOpen={confirmOpen}
          title={api.titles?.create ?? '등록'}
          message="등록하시겠습니까?"
          confirmLabel={isSaving ? '등록 중...' : '등록'}
          onConfirm={async () => {
            setConfirmOpen(false);
            await doCreate();
          }}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </>
  );
}

function EditForm<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
>({
  api,
  fields,
  id,
}: {
  api: FormApiConfig<TValues, TDetail, TCreateRequest, TUpdateRequest>;
  fields: FieldConfig<TValues>[];
  id: number;
}) {
  const detailQuery = api.useGet(id);

  if (detailQuery.isLoading) return <LoadingScreen />;
  if (detailQuery.isError) {
    return (
      <ErrorScreen
        message={getErrorMessage(detailQuery.error)}
        onRetry={detailQuery.refetch}
      />
    );
  }
  if (!detailQuery.data) return null;

  return <EditFormBody api={api} fields={fields} id={id} detail={detailQuery.data} />;
}

function EditFormBody<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
>({
  api,
  fields,
  id,
  detail,
}: {
  api: FormApiConfig<TValues, TDetail, TCreateRequest, TUpdateRequest>;
  fields: FieldConfig<TValues>[];
  id: number;
  detail: TDetail;
}) {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const formState = useFormState<TValues>(api.toValues(detail));
  const [updateFn, { isLoading: isSaving }] = api.useUpdate();
  const [confirmOpen, setConfirmOpen] = useState(false);

  const visibleFields = fields.filter((f) => !f.hideOnEdit);

  const doUpdate = async () => {
    try {
      await updateFn({ id, body: api.toUpdateRequest(formState.values) }).unwrap();
      snackbar.success(api.successMessages?.edit ?? DEFAULT_EDIT_SUCCESS);
      navigate(api.listPath);
    } catch (err) {
      snackbar.error(getErrorMessage(err, DEFAULT_SAVE_ERROR));
    }
  };

  const handleSubmit = async () => {
    if (api.confirm) {
      setConfirmOpen(true);
      return;
    }
    await doUpdate();
  };

  return (
    <>
      <FormBody
        menuCode={api.menuCode}
        fields={visibleFields}
        formState={formState}
        isSaving={isSaving}
        mode="edit"
        section={api.section}
        onSubmit={handleSubmit}
        onCancel={() => navigate(api.listPath)}
      />
      {api.confirm && (
        <ConfirmModal
          isOpen={confirmOpen}
          title={api.titles?.edit ?? '저장'}
          message="저장하시겠습니까?"
          confirmLabel={isSaving ? '저장 중...' : '저장'}
          onConfirm={async () => {
            setConfirmOpen(false);
            await doUpdate();
          }}
          onCancel={() => setConfirmOpen(false)}
        />
      )}
    </>
  );
}

interface FormBodyProps<TValues extends object> {
  menuCode: string;
  fields: FieldConfig<TValues>[];
  formState: FormState<TValues>;
  isSaving: boolean;
  mode: 'create' | 'edit';
  section?: FormSectionInfo;
  onSubmit: () => void;
  onCancel: () => void;
}

function FormBody<TValues extends object>({
  menuCode,
  fields,
  formState,
  isSaving,
  mode,
  section,
  onSubmit,
  onCancel,
}: FormBodyProps<TValues>) {
  const handleFormSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    onSubmit();
  };

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: onCancel, disabled: isSaving },
          {
            design: mode === 'create' ? 'create' : 'save',
            formId: FORM_ID,
            loading: isSaving,
            menuCode,
          },
        ]}
      />

      <FormRoot>
        <FormSurface id={FORM_ID} onSubmit={handleFormSubmit}>
          <FormSection
            icon={section?.icon}
            title={section?.title}
            description={section?.description}
          >
            <FormGrid>
              {fields.map((field) => (
                <FormField
                  key={field.key}
                  field={field}
                  value={(formState.values as Record<string, unknown>)[field.key]}
                  onChange={(v) =>
                    formState.updateField(
                      field.key as keyof TValues,
                      v as TValues[keyof TValues],
                    )
                  }
                  disabled={mode === 'edit' && field.disabledOnEdit === true}
                  mode={mode}
                />
              ))}
            </FormGrid>
          </FormSection>
        </FormSurface>
      </FormRoot>
    </>
  );
}

function DetailForm<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
>({
  api,
  fields,
  id,
}: {
  api: FormApiConfig<TValues, TDetail, TCreateRequest, TUpdateRequest>;
  fields: FieldConfig<TValues>[];
  id: number;
}) {
  const detailQuery = api.useGet(id);

  if (detailQuery.isLoading) return <LoadingScreen />;
  if (detailQuery.isError) {
    return (
      <ErrorScreen
        message={getErrorMessage(detailQuery.error)}
        onRetry={detailQuery.refetch}
      />
    );
  }
  if (!detailQuery.data) return null;

  return <DetailFormBody api={api} fields={fields} id={id} detail={detailQuery.data} />;
}

function DetailFormBody<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
>({
  api,
  fields,
  id,
  detail,
}: {
  api: FormApiConfig<TValues, TDetail, TCreateRequest, TUpdateRequest>;
  fields: FieldConfig<TValues>[];
  id: number;
  detail: TDetail;
}) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(api.menuCode);
  const values = api.toValues(detail);
  const visibleFields = fields.filter((f) => !f.hideOnEdit);

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '목록으로',
            icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(api.listPath),
          },
          ...(canWrite
            ? [
                {
                  design: 'create' as const,
                  label: '수정',
                  icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
                  onClick: () => navigate(`${api.listPath}/${id}/edit`),
                  menuCode: api.menuCode,
                },
              ]
            : []),
        ]}
      />

      <FormRoot>
        <FormSurface as="div" sx={readOnlyTextSx}>
          <FormSection
            icon={api.section?.icon}
            title={api.section?.title}
            description={api.section?.description}
          >
            <FormGrid>
              {visibleFields.map((field) => (
                <FormField
                  key={field.key}
                  field={field}
                  value={(values as Record<string, unknown>)[field.key]}
                  onChange={() => undefined}
                  disabled
                  mode="detail"
                />
              ))}
            </FormGrid>
          </FormSection>
        </FormSurface>
      </FormRoot>
    </>
  );
}

/**
 * 상세 페이지의 disabled 컨트롤 — 배경 회색 톤은 유지하되 글자색은 정상색으로 복원.
 * 브라우저는 disabled input 텍스트에 -webkit-text-fill-color 를 강제 적용하므로 함께 override.
 */
const readOnlyTextSx = (theme: Theme) => ({
  '& .MuiInputBase-input.Mui-disabled, & .MuiSelect-select.Mui-disabled': {
    WebkitTextFillColor: theme.palette.text.primary,
    color: theme.palette.text.primary,
  },
  '& .MuiInputLabel-root.Mui-disabled': {
    color: theme.palette.text.secondary,
  },
});
