import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteContractMutation,
  useDeleteContractsMutation,
  useDownloadContractsExcel,
  useGetContractsQuery,
} from '@/features/contract/api/contractApi';
import {
  type ContractListFilters,
  type ContractSummary,
} from '@/features/contract/types';

/**
 * 계약 목록 page hook — api + headerActions 묶음.
 */
export function useContractListPage(): {
  api: ListApiConfig<ContractSummary, ContractListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<ContractSummary, ContractListFilters> = {
    menuCode: MENU_CODE.CONTRACTS,
    useList: useGetContractsQuery,
    useDelete: useDeleteContractMutation,
    useBulkDelete: useDeleteContractsMutation,
    useExcel: useDownloadContractsExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.CONTRACTS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.CONTRACTS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '계약 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.CONTRACTS]}/new`),
      menuCode: MENU_CODE.CONTRACTS,
    },
  ];

  return { api, headerActions };
}
