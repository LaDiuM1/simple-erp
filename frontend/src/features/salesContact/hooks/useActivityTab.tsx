import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  InlineLinkButton,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import { type DetailModalField } from '@/shared/ui/GenericDetailModal';
import Muted from '@/shared/ui/atoms/Muted';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import type { SalesActivity } from '@/features/salesCustomer/types';
import {
  activityCommonColumns,
  activityHeadDetailFields,
  activityOurEmployeeColumns,
  activityOurEmployeeDetailFields,
} from '@/features/salesCustomer/hooks/salesActivityColumns';
import type { ActivityTabModalProps } from '@/features/salesContact/components/ActivityTabModals/ActivityTabModals';

/**
 * 영업 명부 상세의 활동 탭 — read-only (등록/수정/삭제는 고객사 영업 관리 페이지에서).
 * 셀 텍스트는 ellipsis 로 잘리고 행 클릭 시 GenericDetailModal 로 풀 컨텐츠 노출.
 *
 * activities 데이터는 outer 가 fetch 후 전달. Hook 은 JSX 반환하지 않음 (CLAUDE.md).
 */
export function useActivityTab(
  activities: SalesActivity[],
): { tab: AnyTabbedTab; modal: ActivityTabModalProps } {
  const navigate = useNavigate();
  const [detailTarget, setDetailTarget] = useState<SalesActivity | null>(null);

  const customerColumn: TabbedTableColumn<SalesActivity> = {
    key: 'customer',
    header: '고객사',
    width: 200,
    render: (a) =>
      a.customerName ? (
        <InlineLinkButton
          type="button"
          onClick={(ev) => {
            ev.stopPropagation();
            navigate(`${MENU_PATH[MENU_CODE.SALES_CUSTOMERS]}/${a.customerId}`);
          }}
          sx={{ color: 'inherit', fontWeight: 'inherit' }}
        >
          {a.customerName}
        </InlineLinkButton>
      ) : (
        <Muted />
      ),
  };

  const columns: TabbedTableColumn<SalesActivity>[] = [
    ...activityCommonColumns,
    customerColumn,
    ...activityOurEmployeeColumns,
  ];

  const tab: TabbedTab<SalesActivity> = {
    key: 'activities',
    label: '영업 활동 이력',
    count: activities.length,
    rows: activities,
    rowKey: (a) => a.id,
    columns,
    emptyMessage: '등록된 영업 활동이 없습니다.',
    onRowClick: (a) => setDetailTarget(a),
  };

  const detailFields: DetailModalField[] = detailTarget
    ? [
        ...activityHeadDetailFields(detailTarget),
        { label: '고객사', value: detailTarget.customerName },
        ...activityOurEmployeeDetailFields(detailTarget),
      ]
    : [];

  const modal: ActivityTabModalProps = {
    detailTarget,
    detailFields,
    onCloseDetail: () => setDetailTarget(null),
  };

  return { tab: tabbedTab(tab), modal };
}
