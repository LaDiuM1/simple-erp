import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import TextField from '@mui/material/TextField';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import type { Theme } from '@mui/material/styles';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { FormSection } from '@/shared/ui/GenericForm';
import { usePermission } from '@/shared/hooks/usePermission';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { getErrorMessage } from '@/shared/api/error';
import { useGetEmployeeQuery } from '@/features/employee/api/employeeApi';
import {
  employeeDetailToFormValues,
  type EmployeeDetail,
  type EmployeeFormValues,
} from '@/features/employee/types';
import { employeeBaseValidators } from '@/features/employee/validation/employeeFormValidation';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import BasicInfoSection from '../employeeForm/BasicInfoSection';
import AffiliationSection from '../employeeForm/AffiliationSection';
import AddressSection from '../employeeForm/AddressSection';
import { CreateForm, CreateRoot, FieldGrid } from '../employeeForm/employeeForm.styles';

export default function EmployeeDetailForm({ id }: { id: number }) {
  const { data, isLoading, isError, error, refetch } = useGetEmployeeQuery(id);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  if (!data) return null;

  return <EmployeeDetailFormBody id={id} detail={data} />;
}

function EmployeeDetailFormBody({ id, detail }: { id: number; detail: EmployeeDetail }) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.EMPLOYEES);

  // 폼 섹션이 그대로 재사용되도록 동일한 form state 시그니처 충족 — 다만 readOnly 라 update 등은 호출되지 않는다.
  const values: EmployeeFormValues = useMemo(() => employeeDetailToFormValues(detail), [detail]);
  const validation = useFieldValidation(values, employeeBaseValidators);

  const form: EmployeeFormStateBase = {
    values,
    update: () => undefined,
    validation,
    handleAddressSearch: () => undefined,
  };

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '목록으로',
            icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(MENU_PATH[MENU_CODE.EMPLOYEES]),
          },
          ...(canWrite
            ? [
                {
                  design: 'create' as const,
                  label: '수정',
                  icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
                  onClick: () => navigate(`${MENU_PATH[MENU_CODE.EMPLOYEES]}/${id}/edit`),
                  menuCode: MENU_CODE.EMPLOYEES,
                },
              ]
            : []),
        ]}
      />

      <CreateRoot>
        <CreateForm onSubmit={(e) => e.preventDefault()} noValidate sx={readOnlyTextSx}>
          <AccountInfoReadOnly loginId={values.loginId} />
          <BasicInfoSection form={form} readOnly />
          <AffiliationSection form={form} showStatus readOnly />
          <AddressSection form={form} readOnly />
        </CreateForm>
      </CreateRoot>
    </>
  );
}

/** 상세 페이지의 계정 섹션 — 비밀번호 필드는 숨기고 로그인 ID 만 표시. */
function AccountInfoReadOnly({ loginId }: { loginId: string }) {
  return (
    <FormSection
      icon={<LockRoundedIcon sx={{ fontSize: 18 }} />}
      title="계정 정보"
      description="이 직원의 로그인 ID 입니다."
    >
      <FieldGrid>
        <TextField size="small" label="로그인 ID" value={loginId} disabled />
      </FieldGrid>
    </FormSection>
  );
}

/**
 * 상세 페이지의 disabled 컨트롤 — 배경 회색 톤은 유지하되 글자색은 정상색으로 복원해 가독성 확보.
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
