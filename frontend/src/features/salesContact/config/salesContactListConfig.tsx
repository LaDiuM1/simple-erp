import { useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type CellContext,
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
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
import type { AcquisitionSourceInfo } from '@/features/acquisitionSource/types';
import AcquisitionSourceSearchField from '@/features/acquisitionSource/components/AcquisitionSourceSearchField';

/**
 * sourceIds 필터가 걸려 있으면 매치된 ID Set, 아니면 null.
 * 컨택 경로 컬럼의 매치 우선 정렬 / 강조에 공통 사용.
 */
function activeSourceIdSet(ctx: CellContext): Set<number> | null {
  const ids = (ctx.filters as SalesContactListFilters).sourceIds;
  return Array.isArray(ids) && ids.length > 0 ? new Set(ids) : null;
}

/** 매치된 source 를 앞쪽으로 안정 정렬 (필터 미적용 시 원본 순서 유지). */
function orderSources(
  sources: AcquisitionSourceInfo[],
  ctx: CellContext,
): AcquisitionSourceInfo[] {
  const active = activeSourceIdSet(ctx);
  if (!active) return sources;
  return [...sources].sort((a, b) => Number(active.has(b.id)) - Number(active.has(a.id)));
}

export const salesContactColumn: ColumnConfig<SalesContactSummary>[] = [
  {
    key: 'name',
    label: '이름',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.name}
      </Typography>
    ),
  },
  { key: 'currentCompanyName', label: '현재 소속', flex: 1.2 },
  { key: 'currentPosition', label: '직책', hideOnMobile: true, flex: 1 },
  { key: 'currentDepartment', label: '부서', hideOnMobile: true, flex: 1 },
  { key: 'mobilePhone', label: '휴대폰', flex: 1.5 },
  { key: 'email', label: '이메일', hideOnMobile: true, flex: 1.75 },
  {
    key: 'sources',
    label: '컨택 경로',
    hideOnMobile: true,
    flex: 1.5,
    tooltip: (m, ctx) => {
      if (m.sources.length === 0) return undefined;
      return orderSources(m.sources, ctx).map((s) => s.name).join(', ');
    },
    render: (m, ctx) => {
      if (m.sources.length === 0) return null;
      const activeIds = activeSourceIdSet(ctx);
      const ordered = orderSources(m.sources, ctx);
      return (
        <>
          {ordered.map((s, idx) => {
            const matched = activeIds?.has(s.id) ?? false;
            return (
              <span key={s.id}>
                {idx > 0 && ', '}
                <Box
                  component="span"
                  sx={{
                    color: matched ? 'primary.main' : 'text.primary',
                    fontWeight: matched ? 500 : 400,
                  }}
                >
                  {s.name}
                </Box>
              </span>
            );
          })}
        </>
      );
    },
  },
  {
    key: 'metAt',
    label: '최초 미팅일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
    width: 150,
  },
];

export const salesContactSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'nameKeyword', placeholder: '이름 검색' },
  { type: 'search', key: 'emailKeyword', placeholder: '이메일 검색' },
  { type: 'search', key: 'phoneKeyword', placeholder: '전화번호 검색' },
  {
    type: 'custom',
    key: 'sourceIds',
    render: ({ value, onChange }) => (
      <AcquisitionSourceSearchField
        mode="filter"
        value={Array.isArray(value) ? (value as number[]) : []}
        onChange={(ids) => onChange(ids.length === 0 ? null : ids)}
        label="컨택 경로"
        placeholder="검색하여 추가"
      />
    ),
  },
];

export function useSalesContactListApi(): ListApiConfig<SalesContactSummary, SalesContactListFilters> {
  const navigate = useNavigate();
  return {
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
}
