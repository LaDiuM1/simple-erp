import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeleteSalesContactMutation,
  useDeleteSalesContactsMutation,
  useGetSalesContactsQuery,
} from '@/features/salesContact/api/salesContactApi';
import {
  type SalesContactListFilters,
  type SalesContactSummary,
} from '@/features/salesContact/types';

export const salesContactColumn: ColumnConfig<SalesContactSummary>[] = [
  {
    key: 'name',
    label: '이름',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.name}
      </Typography>
    ),
  },
  { key: 'currentCompanyName', label: '현재 회사' },
  { key: 'currentPosition', label: '직책', hideOnMobile: true },
  { key: 'currentDepartment', label: '부서', hideOnMobile: true },
  { key: 'mobilePhone', label: '휴대폰' },
  { key: 'email', label: '이메일', hideOnMobile: true },
  {
    key: 'metAt',
    label: '최초 미팅일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
  },
];

export const salesContactSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'nameKeyword', placeholder: '이름 검색' },
  { type: 'search', key: 'emailKeyword', placeholder: '이메일 검색' },
  { type: 'search', key: 'phoneKeyword', placeholder: '전화번호 검색' },
];

export function useSalesContactListApi(): ListApiConfig<SalesContactSummary, SalesContactListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.SALES_CONTACTS,
    useList: useGetSalesContactsQuery,
    useDelete: useDeleteSalesContactMutation,
    useBulkDelete: useDeleteSalesContactsMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${m.id}`),
  };
}
