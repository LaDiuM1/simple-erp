import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import type { Theme } from '@mui/material/styles';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { usePermission } from '@/shared/hooks/usePermission';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useGetCustomerQuery } from '@/features/customer/api/customerApi';
import {
  customerDetailToFormValues,
  type CustomerDetail,
  type CustomerFormValues,
} from '@/features/customer/types';
import { customerValidators } from '@/features/customer/validation/customerFormValidation';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import IdentitySection from '../customerForm/IdentitySection';
import ClassificationSection from '../customerForm/ClassificationSection';
import ContactSection from '../customerForm/ContactSection';
import AddressSection from '../customerForm/AddressSection';
import { CreateForm, CreateRoot } from '../customerForm/customerForm.styles';
import type { ApiError } from '@/shared/types/api';

export default function CustomerDetailForm({ id }: { id: number }) {
  const { data, isLoading, isError, error, refetch } = useGetCustomerQuery(id);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  if (!data) return null;

  return <CustomerDetailFormBody id={id} detail={data} />;
}

function CustomerDetailFormBody({ id, detail }: { id: number; detail: CustomerDetail }) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.CUSTOMERS);

  // 폼 섹션이 그대로 재사용되도록 동일한 form state 시그니처 충족 — 다만 readOnly 라 update 등은 호출되지 않는다.
  const values: CustomerFormValues = useMemo(() => customerDetailToFormValues(detail), [detail]);
  const validation = useFieldValidation(values, customerValidators);

  const form: CustomerFormStateBase = {
    values,
    update: () => undefined,
    validation,
    handleAddressSearch: () => undefined,
    bizRegNoStatus: 'idle',
  };

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '목록으로',
            icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(MENU_PATH[MENU_CODE.CUSTOMERS]),
          },
          ...(canWrite
            ? [
                {
                  design: 'create' as const,
                  label: '수정',
                  icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
                  onClick: () => navigate(`${MENU_PATH[MENU_CODE.CUSTOMERS]}/${id}/edit`),
                  menuCode: MENU_CODE.CUSTOMERS,
                },
              ]
            : []),
        ]}
      />

      <CreateRoot>
        <CreateForm onSubmit={(e) => e.preventDefault()} noValidate sx={readOnlyTextSx}>
          <IdentitySection form={form} mode="detail" readOnly />
          <ClassificationSection form={form} readOnly />
          <ContactSection form={form} readOnly />
          <AddressSection form={form} readOnly />
        </CreateForm>
      </CreateRoot>
    </>
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
