import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteSupplierMutation,
  useDeleteSuppliersMutation,
  useGetSuppliersSummaryQuery,
} from '@/features/supplier/api/supplierApi';
import {
  type SupplierListFilters,
  type SupplierSummary,
} from '@/features/supplier/types';

/**
 * 공급사 목록 page hook — api + headerActions 묶음. 페이지는 destructure 후 GenericList 에 prop 전달.
 */
export function useSupplierListPage(): {
  api: ListApiConfig<SupplierSummary, SupplierListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<SupplierSummary, SupplierListFilters> = {
    menuCode: MENU_CODE.SUPPLIERS,
    useList: useGetSuppliersSummaryQuery,
    useDelete: useDeleteSupplierMutation,
    useBulkDelete: useDeleteSuppliersMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.SUPPLIERS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.SUPPLIERS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '공급사 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.SUPPLIERS]}/new`),
      menuCode: MENU_CODE.SUPPLIERS,
    },
  ];

  return { api, headerActions };
}
