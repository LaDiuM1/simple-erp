import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import Muted from '@/shared/ui/atoms/Muted';
import ApprovalStatusIndicator from '@/features/approval/components/ApprovalStatusIndicator';
import {
  APPROVAL_BOX_OPTIONS,
  APPROVAL_DOC_TYPE_LABELS,
  APPROVAL_DOC_TYPE_OPTIONS,
  APPROVAL_STATUS_OPTIONS,
  type ApprovalSummary,
} from '@/features/approval/types';

export const approvalListColumns: ColumnConfig<ApprovalSummary>[] = [
  {
    key: 'docType',
    label: '유형',
    width: 110,
    render: (a) => APPROVAL_DOC_TYPE_LABELS[a.docType],
  },
  {
    key: 'title',
    label: '제목',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 2.5,
    render: (a) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {a.title}
      </Typography>
    ),
  },
  { key: 'drafterName', label: '기안자', flex: 1 },
  {
    key: 'currentStepOrder',
    label: '진행 단계',
    hideOnMobile: true,
    width: 110,
    render: (a) =>
      a.status === 'IN_PROGRESS' ? `${a.currentStepOrder} / ${a.totalSteps}차` : <Muted />,
  },
  {
    key: 'status',
    label: '상태',
    width: 100,
    render: (a) => <ApprovalStatusIndicator status={a.status} />,
  },
  {
    key: 'createdAt',
    label: '기안일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
    width: 150,
    render: (a) => formatDateTime(a.createdAt),
  },
];

export const approvalListFilters: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '제목 검색' },
  { type: 'select', key: 'box', label: '결재함', options: APPROVAL_BOX_OPTIONS, defaultValue: 'PENDING', minWidth: 130 },
  { type: 'select', key: 'status', label: '상태', options: APPROVAL_STATUS_OPTIONS, minWidth: 120 },
  { type: 'select', key: 'docType', label: '유형', options: APPROVAL_DOC_TYPE_OPTIONS, minWidth: 130 },
];
