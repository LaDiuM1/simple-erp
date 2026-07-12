import { useNavigate } from 'react-router-dom';
import EngineeringRoundedIcon from '@mui/icons-material/EngineeringRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteAfterServiceMutation,
  useDeleteAfterServicesMutation,
  useDownloadAfterServicesExcel,
  useGetAfterServicesQuery,
} from '@/features/afterService/api/afterServiceApi';
import {
  type AfterServiceListFilters,
  type AfterServiceSummary,
} from '@/features/afterService/types';

/**
 * AS 목록 page hook — api + headerActions 묶음.
 * 엔지니어 관리는 AS 관리의 서브 기능 — 접수 버튼 옆 secondary 버튼으로 진입
 * (제품 카테고리 관리와 동일 패턴).
 */
export function useAfterServiceListPage(): {
  api: ListApiConfig<AfterServiceSummary, AfterServiceListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.AFTER_SERVICES);

  const api: ListApiConfig<AfterServiceSummary, AfterServiceListFilters> = {
    menuCode: MENU_CODE.AFTER_SERVICES,
    useList: useGetAfterServicesQuery,
    useDelete: useDeleteAfterServiceMutation,
    useBulkDelete: useDeleteAfterServicesMutation,
    useExcel: useDownloadAfterServicesExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    ...(canWrite
      ? [
          {
            design: 'secondary' as const,
            label: '엔지니어 관리',
            icon: <EngineeringRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/engineers`),
          },
        ]
      : []),
    {
      design: 'create',
      label: 'AS 접수',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/new`),
      menuCode: MENU_CODE.AFTER_SERVICES,
    },
  ];

  return { api, headerActions };
}
