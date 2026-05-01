import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import RouteRoundedIcon from '@mui/icons-material/RouteRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteSalesContactMutation,
  useDeleteSalesContactsMutation,
  useDownloadSalesContactsExcel,
  useDownloadSalesContactsTemplate,
  useGetSalesContactsQuery,
  useUploadSalesContactsExcelMutation,
} from '@/features/salesContact/api/salesContactApi';
import {
  type SalesContactListFilters,
  type SalesContactSummary,
} from '@/features/salesContact/types';

/**
 * 영업 명부 목록 page hook — api + headerActions + 컨택 경로 관리 모달 state.
 */
export function useSalesContactListPage(): {
  api: ListApiConfig<SalesContactSummary, SalesContactListFilters>;
  headerActions: PageHeaderAction[];
  manageModal: { open: boolean; onClose: () => void };
} {
  const navigate = useNavigate();
  const [manageOpen, setManageOpen] = useState(false);

  const api: ListApiConfig<SalesContactSummary, SalesContactListFilters> = {
    menuCode: MENU_CODE.SALES_CONTACTS,
    useList: useGetSalesContactsQuery,
    useDelete: useDeleteSalesContactMutation,
    useBulkDelete: useDeleteSalesContactsMutation,
    useExcel: useDownloadSalesContactsExcel,
    useExcelTemplate: useDownloadSalesContactsTemplate,
    useExcelUpload: useUploadSalesContactsExcelMutation,
    excelUploadTitle: '영업 명부 엑셀 업로드',
    excelUploadGuide: (
      <>
        <div><strong>·</strong> [*] 표시는 필수 입력 항목입니다.</div>
        <div><strong>·</strong> 입력 형식 및 예시는 양식 내 안내 시트를 참고해 주세요.</div>
        <div><strong>·</strong> 만난 경로: 사전 등록된 경로명만 입력 가능 (여러 항목 입력 시 콤마로 구분)</div>
        <div><strong>·</strong> 현재 회사: 입력 시 해당 회사의 '현재 재직 중' 이력 자동 생성</div>
      </>
    ),
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'secondary',
      label: '컨택 경로 관리',
      icon: <RouteRoundedIcon />,
      onClick: () => setManageOpen(true),
    },
    {
      design: 'create',
      label: '명부 등록',
      onClick: () => navigate('/sales-contacts/new'),
      menuCode: MENU_CODE.SALES_CONTACTS,
    },
  ];

  return {
    api,
    headerActions,
    manageModal: {
      open: manageOpen,
      onClose: () => setManageOpen(false),
    },
  };
}
