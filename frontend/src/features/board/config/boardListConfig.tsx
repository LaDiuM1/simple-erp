import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import {
  BOARD_CATEGORY_LABELS,
  BOARD_CATEGORY_OPTIONS,
  type BoardCategory,
  type PostSummary,
} from '@/features/board/types';

/** 카테고리 시각 차등 — chip/badge 미사용, 텍스트 색만 (매트릭스 톤 유지). */
const CATEGORY_COLOR: Record<BoardCategory, string> = {
  NOTICE: 'primary.main',
  MEETING: 'success.main',
  FREE: 'text.secondary',
};

export const boardListColumns: ColumnConfig<PostSummary>[] = [
  {
    key: 'category',
    label: '카테고리',
    sortable: true,
    sortDirection: 'asc',
    width: 110,
    render: (m) => (
      <Box component="span" sx={{ color: CATEGORY_COLOR[m.category], fontWeight: 500 }}>
        {BOARD_CATEGORY_LABELS[m.category]}
      </Box>
    ),
  },
  {
    key: 'title',
    label: '제목',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 3,
    render: (m) => (
      <>
        <Typography
          component="span"
          sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}
        >
          {m.title}
        </Typography>
        {m.commentCount > 0 && (
          <Box component="span" sx={{ ml: '0.375rem', color: 'primary.main', fontWeight: 500 }}>
            [{m.commentCount}]
          </Box>
        )}
      </>
    ),
  },
  { key: 'authorName', label: '작성자', flex: 1 },
  {
    key: 'createdAt',
    label: '작성일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
    width: 160,
    render: (m) => formatDateTime(m.createdAt),
  },
];

export const boardListFilters: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '제목 검색' },
  { type: 'select', key: 'category', label: '카테고리', options: BOARD_CATEGORY_OPTIONS, minWidth: 120 },
];
