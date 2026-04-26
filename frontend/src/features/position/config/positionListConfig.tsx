import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeletePositionMutation,
  useGetPositionsSummaryQuery,
} from '@/features/position/api/positionApi';
import {
  type PositionListFilters,
  type PositionSummary,
} from '@/features/position/types';

export const positionColumn: ColumnConfig<PositionSummary>[] = [
  {
    key: 'rankLevel',
    label: '서열',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    width: 80,
    align: 'center',
  },
  {
    key: 'code',
    label: '직책 코드',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '직책명', sortable: true, sortDirection: 'asc' },
  { key: 'description', label: '설명' },
];

export const positionSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '직책 코드 또는 직책명 검색' },
];

/**
 * 직책 목록용 ListApiConfig 를 생성하는 훅.
 * onEdit 이 navigate 를 사용하므로 컴포넌트 내부에서만 호출 가능.
 */
export function usePositionListApi(): ListApiConfig<PositionSummary, PositionListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.POSITIONS,
    useList: useGetPositionsSummaryQuery,
    useDelete: useDeletePositionMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/${m.id}/edit`),
  };
}
