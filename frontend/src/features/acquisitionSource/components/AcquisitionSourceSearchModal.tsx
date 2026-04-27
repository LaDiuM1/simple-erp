import { useMemo } from 'react';
import Typography from '@mui/material/Typography';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import type { CommonSearchModalApi, CommonSearchSelectedItem } from '@/shared/ui/CommonSearchModal/types';
import type { ColumnConfig, FilterConfig } from '@/shared/ui/GenericList';
import { useGetAcquisitionSourcesQuery } from '@/features/acquisitionSource/api/acquisitionSourceApi';
import {
  ACQUISITION_SOURCE_TYPE_LABELS,
  ACQUISITION_SOURCE_TYPE_OPTIONS,
  type AcquisitionSourceInfo,
  type AcquisitionSourceType,
} from '@/features/acquisitionSource/types';

interface FilterParams {
  nameKeyword: string | null;
  type: AcquisitionSourceType | null;
}

interface Props {
  open: boolean;
  onClose: () => void;
  /** 폼 / 필터 컨텍스트 — 확인 버튼 라벨만 달라지고 데이터 모델은 동일 (sourceId 만). */
  context: 'form' | 'filter';
  /** 모달 진입 시 미리 선택된 sourceId 목록. */
  initialIds: number[];
  /** 확인 클릭 시 호출 — 선택된 sourceId 목록. */
  onConfirm: (ids: number[]) => void;
}

const COLUMNS: ColumnConfig<AcquisitionSourceInfo>[] = [
  {
    key: 'name',
    label: '이름',
    mobilePrimary: true,
    render: (s) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {s.name}
      </Typography>
    ),
  },
  {
    key: 'type',
    label: '분류',
    render: (s) => ACQUISITION_SOURCE_TYPE_LABELS[s.type],
  },
  { key: 'description', label: '설명' },
];

const SEARCH_FILTER: FilterConfig[] = [
  { type: 'search', key: 'nameKeyword', placeholder: '이름 검색' },
  {
    type: 'select',
    key: 'type',
    label: '분류',
    options: ACQUISITION_SOURCE_TYPE_OPTIONS,
    minWidth: 140,
  },
];

/**
 * 컨택 경로 검색 모달 — 영업 명부 폼 / 목록 필터 양쪽에서 재사용.
 * 행 클릭 → 하단 트레이에 누적 (CommonSearchModal 의 selectionStyle='tray').
 * 데이터: useGetAcquisitionSourcesQuery 의 flat 배열을 클라이언트 측 필터 / 페이징.
 */
export default function AcquisitionSourceSearchModal({
  open,
  onClose,
  context,
  initialIds,
  onConfirm,
}: Props) {
  const { data: allSources = [], isFetching, isError, error, refetch } = useGetAcquisitionSourcesQuery();

  const initialSelected = useMemo<CommonSearchSelectedItem[]>(
    () =>
      initialIds
        .map((id) => allSources.find((s) => s.id === id))
        .filter((s): s is AcquisitionSourceInfo => !!s)
        .map((s) => ({ id: s.id, label: s.name })),
    [initialIds, allSources],
  );

  /** flat 배열 → CommonSearchModal 이 기대하는 페이징 응답 어댑터 (클라이언트 측 필터 + 페이징). */
  const adapterApi: CommonSearchModalApi<AcquisitionSourceInfo, FilterParams> = useMemo(
    () => ({
      useList: (params) => {
        const filtered = filterAndSort(allSources, params);
        const totalElements = filtered.length;
        const totalPages = Math.max(1, Math.ceil(totalElements / params.size));
        const start = params.page * params.size;
        const content = filtered.slice(start, start + params.size);
        return {
          data: {
            content,
            page: params.page,
            size: params.size,
            totalElements,
            totalPages,
            hasNext: start + params.size < totalElements,
          },
          isFetching,
          isError,
          error,
          refetch,
        };
      },
      rowKey: (row) => row.id,
      rowLabel: (row) => row.name,
      pageSize: 8,
    }),
    [allSources, isFetching, isError, error, refetch],
  );

  const handleSelect = (selected: CommonSearchSelectedItem[]) => {
    onConfirm(selected.map((s) => s.id));
  };

  return (
    <CommonSearchModal<AcquisitionSourceInfo, FilterParams>
      open={open}
      onClose={onClose}
      title="컨택 경로 검색"
      mode="select"
      multiple
      selectionStyle="tray"
      api={adapterApi}
      searchFilter={SEARCH_FILTER}
      column={COLUMNS}
      initialSelected={initialSelected}
      onSelect={handleSelect}
      confirmLabel={context === 'filter' ? '검색' : '확인'}
      emptyMessage="검색 결과가 없습니다."
    />
  );
}

function filterAndSort(
  all: AcquisitionSourceInfo[],
  params: FilterParams & { sort: string },
): AcquisitionSourceInfo[] {
  let result = all;
  const keyword = params.nameKeyword?.trim().toLowerCase();
  if (keyword) {
    result = result.filter((s) => s.name.toLowerCase().includes(keyword));
  }
  if (params.type) {
    result = result.filter((s) => s.type === params.type);
  }
  // 분류 → 이름 기본 정렬 (BE findAll 과 동일).
  return [...result].sort((a, b) => {
    if (a.type !== b.type) return a.type.localeCompare(b.type);
    return a.name.localeCompare(b.name);
  });
}
