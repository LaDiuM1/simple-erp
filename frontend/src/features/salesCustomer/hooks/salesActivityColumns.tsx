import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import {
  formatDateTime,
  NowrapText,
  RowActions,
  StatusText,
  TruncatedSpan,
  type StatusTone,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import Muted from '@/shared/ui/atoms/Muted';
import {
  SALES_ACTIVITY_TYPE_LABELS,
  type SalesActivity,
  type SalesActivityType,
} from '@/features/salesCustomer/types';
import type { DetailModalField } from '@/shared/ui/GenericDetailModal';

const ACTIVITY_TYPE_TONE: Record<SalesActivityType, StatusTone> = {
  VISIT: 'primary',
  CALL: 'success',
  MEETING: 'info',
  EMAIL: 'warning',
  OTHER: 'secondary',
};

/** 영업 활동 공통 컬럼 (유형 / 일시 / 제목 / 내용) — contact / customer 양쪽 컨텍스트가 공유. */
export const activityCommonColumns: TabbedTableColumn<SalesActivity>[] = [
  {
    key: 'type',
    header: '유형',
    width: 80,
    render: (a) => (
      <StatusText tone={ACTIVITY_TYPE_TONE[a.type]}>
        {SALES_ACTIVITY_TYPE_LABELS[a.type]}
      </StatusText>
    ),
  },
  {
    key: 'date',
    header: '일시',
    width: 144,
    render: (a) => <NowrapText>{formatDateTime(a.activityDate)}</NowrapText>,
  },
  {
    key: 'subject',
    header: '제목',
    render: (a) => <TruncatedSpan maxWidth={240}>{a.subject}</TruncatedSpan>,
  },
  {
    key: 'content',
    header: '내용',
    render: (a) =>
      a.content ? <TruncatedSpan maxWidth={320}>{a.content}</TruncatedSpan> : <Muted />,
  },
];

/** 우리 담당 / 담당 부서 — contact / customer 양쪽 컨텍스트가 공유. */
export const activityOurEmployeeColumns: TabbedTableColumn<SalesActivity>[] = [
  {
    key: 'employee',
    header: '우리 담당',
    width: 132,
    render: (a) => a.ourEmployeeName ?? <Muted />,
  },
  {
    key: 'employeeDept',
    header: '담당 부서',
    width: 144,
    render: (a) => a.ourEmployeeDepartmentName ?? <Muted />,
  },
];

interface ActionHandlers {
  onEdit: (a: SalesActivity) => void;
  onDelete: (a: SalesActivity) => void;
}

export function activityActionsColumn({
  onEdit,
  onDelete,
}: ActionHandlers): TabbedTableColumn<SalesActivity> {
  return {
    key: 'actions',
    header: '액션',
    align: 'right',
    width: 80,
    render: (a) => (
      <RowActions>
        <Tooltip title="수정" arrow>
          <IconButton
            size="small"
            onClick={(ev) => {
              ev.stopPropagation();
              onEdit(a);
            }}
          >
            <EditOutlinedIcon fontSize="small" />
          </IconButton>
        </Tooltip>
        <Tooltip title="삭제" arrow>
          <IconButton
            size="small"
            onClick={(ev) => {
              ev.stopPropagation();
              onDelete(a);
            }}
            sx={{ '&:hover': { color: 'error.main' } }}
          >
            <DeleteOutlineIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </RowActions>
    ),
  };
}

/** Detail 모달의 활동 공통 head 필드 (유형/일시/제목/내용). 컨텍스트별 필드는 호출자가 사이/끝에 끼움. */
export function activityHeadDetailFields(a: SalesActivity): DetailModalField[] {
  return [
    { label: '유형', value: SALES_ACTIVITY_TYPE_LABELS[a.type] },
    { label: '일시', value: formatDateTime(a.activityDate) },
    { label: '제목', value: a.subject },
    { label: '내용', value: a.content },
  ];
}

/** Detail 모달의 활동 공통 tail 필드 (우리 담당 / 담당 부서). */
export function activityOurEmployeeDetailFields(a: SalesActivity): DetailModalField[] {
  return [
    { label: '우리 담당', value: a.ourEmployeeName },
    { label: '담당 부서', value: a.ourEmployeeDepartmentName },
  ];
}
