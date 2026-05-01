import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteCustomerMutation,
  useDeleteCustomersMutation,
  useDownloadCustomersExcel,
  useDownloadCustomersTemplate,
  useGetCustomersQuery,
  useUploadCustomersExcelMutation,
} from '@/features/customer/api/customerApi';
import {
  type CustomerListFilters,
  type CustomerSummary,
} from '@/features/customer/types';

/**
 * 고객사 목록 page hook — api + headerActions 묶음.
 */
export function useCustomerListPage(): {
  api: ListApiConfig<CustomerSummary, CustomerListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<CustomerSummary, CustomerListFilters> = {
    menuCode: MENU_CODE.CUSTOMERS,
    useList: useGetCustomersQuery,
    useDelete: useDeleteCustomerMutation,
    useBulkDelete: useDeleteCustomersMutation,
    useExcel: useDownloadCustomersExcel,
    useExcelTemplate: useDownloadCustomersTemplate,
    useExcelUpload: useUploadCustomersExcelMutation,
    excelUploadTitle: '고객사 엑셀 업로드',
    excelUploadGuide: (
      <>
        <div><strong>·</strong> [*] 표시는 필수 입력 항목입니다.</div>
        <div><strong>·</strong> 입력 형식 및 예시는 양식 내 안내 시트를 참고해 주세요.</div>
      </>
    ),
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.CUSTOMERS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.CUSTOMERS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '고객사 등록',
      onClick: () => navigate('/customers/new'),
      menuCode: MENU_CODE.CUSTOMERS,
    },
  ];

  return { api, headerActions };
}
