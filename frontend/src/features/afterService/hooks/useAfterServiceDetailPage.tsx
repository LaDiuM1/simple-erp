import { Link as RouterLink, useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Link from '@mui/material/Link';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { formatKrw } from '@/shared/utils/formatKrw';
import { useGetAfterServiceQuery } from '@/features/afterService/api/afterServiceApi';
import ServiceStatusIndicator from '@/features/afterService/components/ServiceStatusIndicator';
import WarrantyDecisionText from '@/features/afterService/components/WarrantyDecisionText';
import {
  SERVICE_TYPE_LABELS,
  equipmentLabelOf,
  type AfterServiceDetail,
} from '@/features/afterService/types';
import { useVisitTab } from './useVisitTab';
import { useExpenseTab } from './useExpenseTab';

/**
 * AS 상세 page hook — fetching / 권한 / 탭 오케스트레이션 / headerActions 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `afterServiceInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useAfterServiceDetailPage(afterServiceId: number) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.AFTER_SERVICES);

  const detailQuery = useGetAfterServiceQuery(afterServiceId, { skip: !afterServiceId });

  const visitTab = useVisitTab(afterServiceId, detailQuery.data?.visits ?? []);
  const expenseTab = useExpenseTab(afterServiceId, detailQuery.data?.expenses ?? []);

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.AFTER_SERVICES]),
    },
    ...(canWrite
      ? [
          {
            design: 'create' as const,
            label: '수정',
            icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
            onClick: () =>
              navigate(`${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/${afterServiceId}/edit`),
            menuCode: MENU_CODE.AFTER_SERVICES,
          },
        ]
      : []),
  ];

  return {
    queries: { detail: detailQuery },
    headerActions,
    tabsList: [visitTab.tab, expenseTab.tab],
    tabs: {
      visit: visitTab.modal,
      expense: expenseTab.modal,
    },
  };
}

export function afterServiceInfoFields(d: AfterServiceDetail): HeaderDetailField[] {
  return [
    { label: '접수번호', value: d.receiptNo },
    { label: '상태', value: <ServiceStatusIndicator status={d.status} /> },
    { label: '고객사', value: d.customerName },
    {
      label: '설비',
      value: d.equipmentId ? (
        <Link
          component={RouterLink}
          to={`${MENU_PATH[MENU_CODE.EQUIPMENTS]}/${d.equipmentId}`}
          underline="hover"
          color="primary"
          sx={{ fontWeight: 500, fontSize: 'inherit' }}
        >
          {equipmentLabelOf(d.equipmentModelName, d.equipmentSerialNo)}
        </Link>
      ) : null,
    },
    { label: '유형', value: SERVICE_TYPE_LABELS[d.type] },
    { label: '주 담당', value: d.assignedEngineerName },
    { label: '접수일', value: d.receivedDate },
    { label: '완료일', value: d.completedDate },
    { label: '유상 / 무상', value: <WarrantyDecisionText decision={d.warrantyDecision} /> },
    { label: '청구액', value: d.billingAmount == null ? null : formatKrw(d.billingAmount) },
    { label: '경비 합계', value: formatKrw(d.expenseTotal) },
    { label: '증상 / 요청 내용', value: d.symptom, fullWidth: true },
  ];
}
