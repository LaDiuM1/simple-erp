import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { useGetSalesCustomerDetailQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import { useGetSalesContactEmploymentsByCustomerIdQuery } from '@/features/salesContact/api/salesContactApi';
import { useGetCustomerQuery } from '@/features/customer/api/customerApi';
import {
  CUSTOMER_STATUS_LABELS,
  CUSTOMER_TYPE_LABELS,
  type CustomerDetail,
} from '@/features/customer/types';
import { useActivityTab } from './useActivityTab';
import { useAssignmentTab } from './useAssignmentTab';
import { useCustomerEmploymentTab } from '@/features/salesContact/hooks/useCustomerEmploymentTab';

/**
 * 영업 고객사 상세 page hook — fetching / 탭 오케스트레이션 / headerActions 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 헤더 필드는 customer 보장된 렌더 시점에
 * `customerInfoFields(customer)` 를 page 가 호출하도록 builder 만 export.
 */
export function useSalesCustomerDetailPage(customerId: number) {
  const navigate = useNavigate();

  const salesQuery = useGetSalesCustomerDetailQuery(customerId, { skip: !customerId });
  const customerQuery = useGetCustomerQuery(customerId, { skip: !customerId });
  const employmentsQuery = useGetSalesContactEmploymentsByCustomerIdQuery(customerId, {
    skip: !customerId,
  });

  const assignmentTab = useAssignmentTab(customerId, salesQuery.data?.assignments ?? []);
  const activityTab = useActivityTab(customerId, salesQuery.data?.activities ?? []);
  const customerEmploymentTab = useCustomerEmploymentTab(employmentsQuery.data ?? []);

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.SALES_CUSTOMERS]),
    },
  ];

  return {
    queries: { sales: salesQuery, customer: customerQuery, employments: employmentsQuery },
    headerActions,
    tabsList: [assignmentTab.tab, activityTab.tab, customerEmploymentTab.tab],
    tabs: {
      assignment: assignmentTab.modal,
      activity: activityTab.modal,
    },
  };
}

export function customerInfoFields(c: CustomerDetail): HeaderDetailField[] {
  const fullAddress = [c.zipCode ? `(${c.zipCode})` : null, c.roadAddress, c.detailAddress]
    .filter(Boolean)
    .join(' ');
  return [
    { label: '고객사 코드', value: c.code },
    { label: '고객사명', value: c.name },
    { label: '전화번호', value: c.phone },
    { label: '대표자', value: c.representative },
    { label: '유형', value: CUSTOMER_TYPE_LABELS[c.type] },
    { label: '상태', value: CUSTOMER_STATUS_LABELS[c.status] },
    { label: '주소', value: fullAddress || null, fullWidth: true },
  ];
}
