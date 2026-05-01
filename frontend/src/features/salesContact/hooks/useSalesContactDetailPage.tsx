import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Link from '@mui/material/Link';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { useGetSalesContactQuery } from '@/features/salesContact/api/salesContactApi';
import { useGetSalesActivitiesByContactQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import type { SalesContactDetail } from '@/features/salesContact/types';
import { useSourceTab } from './useSourceTab';
import { useEmploymentTab } from './useEmploymentTab';
import { useActivityTab } from './useActivityTab';

/**
 * 영업 명부 상세 page hook — fetching / 권한 / 탭 오케스트레이션 / headerActions 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `contactInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useSalesContactDetailPage(contactId: number) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.SALES_CONTACTS);

  const detailQuery = useGetSalesContactQuery(contactId, { skip: !contactId });
  const activitiesQuery = useGetSalesActivitiesByContactQuery(contactId, { skip: !contactId });

  const sourceTab = useSourceTab(detailQuery.data?.sources ?? []);
  const employmentTab = useEmploymentTab(contactId, detailQuery.data?.employments ?? []);
  const activityTab = useActivityTab(activitiesQuery.data ?? []);

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.SALES_CONTACTS]),
    },
    ...(canWrite
      ? [
          {
            design: 'create' as const,
            label: '수정',
            icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${contactId}/edit`),
            menuCode: MENU_CODE.SALES_CONTACTS,
          },
        ]
      : []),
  ];

  return {
    queries: { detail: detailQuery, activities: activitiesQuery },
    headerActions,
    tabsList: [employmentTab.tab, activityTab.tab, sourceTab.tab],
    tabs: {
      employment: employmentTab.modal,
      activity: activityTab.modal,
    },
  };
}

export function contactInfoFields(d: SalesContactDetail): HeaderDetailField[] {
  return [
    { label: '이름', value: d.name },
    { label: '영문명', value: d.nameEn },
    {
      label: '휴대폰',
      value: d.mobilePhone ? (
        <Link
          href={`tel:${d.mobilePhone}`}
          underline="hover"
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.mobilePhone}
        </Link>
      ) : (
        d.mobilePhone
      ),
    },
    {
      label: '전화번호',
      value: d.officePhone ? (
        <Link
          href={`tel:${d.officePhone}`}
          underline="hover"
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.officePhone}
        </Link>
      ) : (
        d.officePhone
      ),
    },
    {
      label: '회사 이메일',
      value: d.email ? (
        <Link
          href={`mailto:${d.email}`}
          underline="hover"
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.email}
        </Link>
      ) : (
        d.email
      ),
    },
    {
      label: '개인 이메일',
      value: d.personalEmail ? (
        <Link
          href={`mailto:${d.personalEmail}`}
          underline="hover"
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.personalEmail}
        </Link>
      ) : (
        d.personalEmail
      ),
    },
    { label: '최초 미팅일', value: d.metAt },
    { label: '비고', value: d.note },
  ];
}
