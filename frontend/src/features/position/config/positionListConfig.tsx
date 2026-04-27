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
  useDeletePositionsMutation,
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
  { type: 'search', key: 'codeKeyword', placeholder: '직책 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '직책명 검색' },
];

/**
 * 직책 목록용 ListApiConfig 를 생성하는 훅.
 * 데스크탑: 행 클릭 → 상세 + 체크박스 일괄 삭제. 모바일: 카드 + 행별 수정/삭제 아이콘.
 */
export function usePositionListApi(): ListApiConfig<PositionSummary, PositionListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.POSITIONS,
    useList: useGetPositionsSummaryQuery,
    useDelete: useDeletePositionMutation,
    useBulkDelete: useDeletePositionsMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/${m.id}/edit`),
  };
}
