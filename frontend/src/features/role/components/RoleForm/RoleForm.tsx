import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import TextField from '@mui/material/TextField';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { FormSection } from '@/shared/ui/GenericForm';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import MenuPermissionMatrix from '@/features/role/components/MenuPermissionMatrix';
import RoleCodeField from './RoleCodeField';
import { ContentBox, FieldFull, FieldsGrid, FormRoot, FormSurface } from './RoleForm.styles';
import type { RoleFormValues } from '@/features/role/types';
import type { RoleErrors } from '@/features/role/validation/roleValidation';

interface BaseProps {
  values: RoleFormValues;
  errors: RoleErrors;
  setField: <K extends keyof RoleFormValues>(key: K, v: RoleFormValues[K]) => void;
  setPermissions: (next: RoleFormValues['permissions']) => void;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

interface CreateProps extends BaseProps {
  mode: 'create';
  setCodeAvailable: (next: boolean | null) => void;
}

interface EditProps extends BaseProps {
  mode: 'edit';
  isSystem: boolean;
}

type Props = CreateProps | EditProps;

const FORM_ID = 'role-form';

/**
 * 권한 등록/수정 공용 폼 레이아웃.
 * 기본정보 섹션 + 메뉴 권한 매트릭스 섹션. 헤더 액션은 portal 로 주입.
 */
export default function RoleForm(props: Props) {
  const {
    values, errors, setField, setPermissions,
    isSaving, confirmOpen, handleSubmit, handleConfirmedSubmit, closeConfirm, handleCancel,
  } = props;

  const isEdit = props.mode === 'edit';
  const isSystem = props.mode === 'edit' && props.isSystem;

  const sectionDescription = isSystem
    ? '시스템 권한입니다. 이름과 설명만 수정할 수 있고, 코드와 메뉴 권한은 변경할 수 없습니다.'
    : '권한 코드와 이름, 메뉴별 읽기/쓰기 권한을 지정합니다.';

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: handleCancel, disabled: isSaving },
          {
            design: 'save',
            formId: FORM_ID,
            loading: isSaving,
            menuCode: MENU_CODE.ROLES,
          },
        ]}
      />

      <FormRoot>
        <FormSurface id={FORM_ID} onSubmit={handleSubmit} noValidate>
          <ContentBox>
            <FormSection
              icon={isSystem ? <LockRoundedIcon sx={{ fontSize: 18 }} /> : <BadgeRoundedIcon sx={{ fontSize: 18 }} />}
              title={isSystem ? '시스템 권한 정보' : '권한 정보'}
              description={sectionDescription}
            >
              <FieldsGrid>
                <FieldFull>
                  {isEdit ? (
                    <TextField
                      label="권한 코드"
                      value={values.code}
                      disabled
                      fullWidth
                      size="small"
                      helperText="코드는 등록 후 변경할 수 없습니다."
                    />
                  ) : (
                    <RoleCodeField
                      value={values.code}
                      onChange={(v) => setField('code', v)}
                      onAvailabilityChange={(props as CreateProps).setCodeAvailable}
                      externalError={errors.code}
                    />
                  )}
                </FieldFull>

                <FieldFull>
                  <TextField
                    label="권한명"
                    value={values.name}
                    onChange={(e) => setField('name', e.target.value)}
                    error={!!errors.name}
                    helperText={errors.name}
                    fullWidth
                    size="small"
                    required
                    slotProps={{ htmlInput: { maxLength: 100 } }}
                  />
                </FieldFull>

                <FieldFull>
                  <TextField
                    label="설명"
                    value={values.description}
                    onChange={(e) => setField('description', e.target.value)}
                    fullWidth
                    size="small"
                    multiline
                    minRows={2}
                    slotProps={{ htmlInput: { maxLength: 500 } }}
                  />
                </FieldFull>
              </FieldsGrid>
            </FormSection>

            <FormSection
              title="메뉴 권한"
              description="메뉴별 읽기/쓰기 권한을 체크합니다. 쓰기를 체크하면 읽기도 자동으로 부여됩니다."
            >
              <MenuPermissionMatrix
                permissions={values.permissions}
                onChange={setPermissions}
                readOnly={isSystem}
              />
            </FormSection>
          </ContentBox>
        </FormSurface>
      </FormRoot>

      <ConfirmModal
        isOpen={confirmOpen}
        title={isEdit ? '권한 수정' : '권한 등록'}
        message={isEdit ? '변경 내용을 저장하시겠습니까?' : '새 권한을 등록하시겠습니까?'}
        confirmLabel={isSaving ? '저장 중...' : '저장'}
        onConfirm={handleConfirmedSubmit}
        onCancel={closeConfirm}
      />
    </>
  );
}
