import { type FormEventHandler } from 'react';
import { useNavigate } from 'react-router-dom';
import CircularProgress from '@mui/material/CircularProgress';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { PrimaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import type { ApiError } from '@/shared/types/api';
import FormField from './FormField';
import {
  CancelHeaderButton,
  FormGrid,
  FormRoot,
  FormSurface,
} from './GenericForm.styles';
import { useFormState } from './useFormState';
import type { FieldConfig, FormApiConfig, FormState } from './types';

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
  /** 수정 모드 대상 ID. 미지정 시 생성 모드. */
  id?: number;
}

/**
 * 도메인 등록/수정 페이지 단일 규격 컴포넌트.
 * `id` 유무로 CreateForm / EditForm 분기 — 각 하위 컴포넌트가 고정된 훅 호출 순서를 가짐.
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
}: GenericFormProps<TValues, TDetail, TCreateRequest, TUpdateRequest>) {
  if (id != null) {
    return <EditForm api={api} fields={fields} id={id} />;
  }
  return <CreateForm api={api} fields={fields} />;
}

/* --------------------------------------------------------------------------
 * Create mode
 * ------------------------------------------------------------------------ */

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

  const visibleFields = fields.filter((f) => !f.hideOnCreate);

  const handleSubmit = async () => {
    try {
      await createFn(api.toCreateRequest(formState.values)).unwrap();
      snackbar.success(api.successMessages?.create ?? DEFAULT_CREATE_SUCCESS);
      navigate(api.listPath);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? DEFAULT_SAVE_ERROR);
    }
  };

  return (
    <FormBody
      menuCode={api.menuCode}
      fields={visibleFields}
      formState={formState}
      isSaving={isSaving}
      mode="create"
      onSubmit={handleSubmit}
      onCancel={() => navigate(api.listPath)}
    />
  );
}

/* --------------------------------------------------------------------------
 * Edit mode — 상세 조회 후 초기값 세팅
 * ------------------------------------------------------------------------ */

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
        message={(detailQuery.error as ApiError)?.message}
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

  const visibleFields = fields.filter((f) => !f.hideOnEdit);

  const handleSubmit = async () => {
    try {
      await updateFn({ id, body: api.toUpdateRequest(formState.values) }).unwrap();
      snackbar.success(api.successMessages?.edit ?? DEFAULT_EDIT_SUCCESS);
      navigate(api.listPath);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? DEFAULT_SAVE_ERROR);
    }
  };

  return (
    <FormBody
      menuCode={api.menuCode}
      fields={visibleFields}
      formState={formState}
      isSaving={isSaving}
      mode="edit"
      onSubmit={handleSubmit}
      onCancel={() => navigate(api.listPath)}
    />
  );
}

/* --------------------------------------------------------------------------
 * Shared body (그리드 + 제출/취소)
 * ------------------------------------------------------------------------ */

interface FormBodyProps<TValues extends object> {
  menuCode: string;
  fields: FieldConfig<TValues>[];
  formState: FormState<TValues>;
  isSaving: boolean;
  mode: 'create' | 'edit';
  onSubmit: () => void;
  onCancel: () => void;
}

function FormBody<TValues extends object>({
  menuCode,
  fields,
  formState,
  isSaving,
  mode,
  onSubmit,
  onCancel,
}: FormBodyProps<TValues>) {
  const handleFormSubmit: FormEventHandler<HTMLFormElement> = (e) => {
    e.preventDefault();
    if (isSaving) return;
    onSubmit();
  };

  return (
    <>
      <PageHeaderActions>
        <CancelHeaderButton
          type="button"
          variant="outlined"
          onClick={onCancel}
          disabled={isSaving}
        >
          취소
        </CancelHeaderButton>
        <PermissionGate menuCode={menuCode} action="write">
          <PrimaryPageHeaderButton
            type="submit"
            form={FORM_ID}
            disabled={isSaving}
            startIcon={
              isSaving ? (
                <CircularProgress size={14} color="inherit" />
              ) : mode === 'create' ? (
                <AddIcon />
              ) : (
                <SaveIcon />
              )
            }
          >
            {mode === 'create' ? '등록' : '저장'}
          </PrimaryPageHeaderButton>
        </PermissionGate>
      </PageHeaderActions>

      <FormRoot>
        <FormSurface id={FORM_ID} onSubmit={handleFormSubmit}>
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
              />
            ))}
          </FormGrid>
        </FormSurface>
      </FormRoot>
    </>
  );
}
