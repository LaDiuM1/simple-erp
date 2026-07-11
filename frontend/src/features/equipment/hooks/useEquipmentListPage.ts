import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteEquipmentMutation,
  useDeleteEquipmentsMutation,
  useDownloadEquipmentsExcel,
  useGetEquipmentsQuery,
} from '@/features/equipment/api/equipmentApi';
import {
  type EquipmentListFilters,
  type EquipmentSummary,
} from '@/features/equipment/types';

/**
 * 설비 대장 목록 page hook — api + headerActions 묶음.
 */
export function useEquipmentListPage(): {
  api: ListApiConfig<EquipmentSummary, EquipmentListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<EquipmentSummary, EquipmentListFilters> = {
    menuCode: MENU_CODE.EQUIPMENTS,
    useList: useGetEquipmentsQuery,
    useDelete: useDeleteEquipmentMutation,
    useBulkDelete: useDeleteEquipmentsMutation,
    useExcel: useDownloadEquipmentsExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.EQUIPMENTS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.EQUIPMENTS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '설비 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.EQUIPMENTS]}/new`),
      menuCode: MENU_CODE.EQUIPMENTS,
    },
  ];

  return { api, headerActions };
}
