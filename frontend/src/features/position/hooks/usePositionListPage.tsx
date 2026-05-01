import { useNavigate } from 'react-router-dom';
import LowPriorityRoundedIcon from '@mui/icons-material/LowPriorityRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeletePositionMutation,
  useDeletePositionsMutation,
  useGetPositionsSummaryQuery,
} from '@/features/position/api/positionApi';
import {
  type PositionListFilters,
  type PositionSummary,
} from '@/features/position/types';

/**
 * 직책 목록 page hook — api + headerActions 묶음.
 */
export function usePositionListPage(): {
  api: ListApiConfig<PositionSummary, PositionListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<PositionSummary, PositionListFilters> = {
    menuCode: MENU_CODE.POSITIONS,
    useList: useGetPositionsSummaryQuery,
    useDelete: useDeletePositionMutation,
    useBulkDelete: useDeletePositionsMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'secondary',
      label: '직책 서열 관리',
      icon: <LowPriorityRoundedIcon />,
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/ranking`),
    },
    {
      design: 'create',
      label: '직책 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/new`),
      menuCode: MENU_CODE.POSITIONS,
    },
  ];

  return { api, headerActions };
}
