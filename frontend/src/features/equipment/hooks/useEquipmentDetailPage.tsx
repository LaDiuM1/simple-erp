import { Link as RouterLink, useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Link from '@mui/material/Link';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { useGetEquipmentQuery } from '@/features/equipment/api/equipmentApi';
import WarrantyDateText from '@/features/equipment/components/WarrantyDateText';
import { formatOutput, type EquipmentDetail } from '@/features/equipment/types';

/**
 * 설비 상세 page hook — fetching / 권한 / headerActions 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `equipmentInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useEquipmentDetailPage(equipmentId: number) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.EQUIPMENTS);

  const detailQuery = useGetEquipmentQuery(equipmentId, { skip: !equipmentId });

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.EQUIPMENTS]),
    },
    ...(canWrite
      ? [
          {
            design: 'create' as const,
            label: '수정',
            icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.EQUIPMENTS]}/${equipmentId}/edit`),
            menuCode: MENU_CODE.EQUIPMENTS,
          },
        ]
      : []),
  ];

  return {
    queries: { detail: detailQuery },
    headerActions,
  };
}

export function equipmentInfoFields(d: EquipmentDetail): HeaderDetailField[] {
  return [
    { label: '고객사', value: d.customerName },
    {
      label: '연결 계약',
      value: d.contractId ? (
        <Link
          component={RouterLink}
          to={`${MENU_PATH[MENU_CODE.CONTRACTS]}/${d.contractId}`}
          underline="hover"
          color="primary"
          sx={{ fontWeight: 500, fontSize: 'inherit' }}
        >
          {d.contractNo ?? '계약 상세 보기'}
        </Link>
      ) : null,
    },
    { label: '유형', value: d.categoryName },
    { label: '모델명', value: d.productModelName },
    { label: '공급사', value: d.supplierName },
    { label: '출력', value: formatOutput(d.outputValue, d.outputUnit) },
    { label: '시리얼 번호', value: d.serialNo },
    { label: '설치 주소', value: d.installAddress },
    { label: '설치일', value: d.installedDate },
    { label: '설치완료확인서 일자', value: d.confirmedDate },
    { label: '보증 기산일', value: d.warrantyStartDate },
    { label: '보증보험', value: d.warrantyInsurance ? '가입' : '미가입' },
    {
      label: '발진기 보증',
      value: d.oscillatorWarrantyMonths == null
        ? null
        : `${d.oscillatorWarrantyMonths}개월`,
    },
    { label: '발진기 보증 만료일', value: <WarrantyDateText endDate={d.oscillatorWarrantyEndDate} /> },
    {
      label: '그외 무상 AS',
      value: d.generalWarrantyMonths == null
        ? null
        : `${d.generalWarrantyMonths}개월`,
    },
    { label: '무상 AS 만료일', value: <WarrantyDateText endDate={d.generalWarrantyEndDate} /> },
    { label: '비고', value: d.note, fullWidth: true },
  ];
}
