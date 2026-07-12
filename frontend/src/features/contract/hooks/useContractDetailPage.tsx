import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { formatKrw } from '@/shared/utils/formatKrw';
import { useGetContractQuery } from '@/features/contract/api/contractApi';
import ContractStatusIndicator from '@/features/contract/components/ContractStatusIndicator';
import {
  SUPPORT_PROGRAM_STATUS_LABELS,
  formatOutput,
  type ContractDetail,
} from '@/features/contract/types';
import { usePaymentTab } from './usePaymentTab';
import { useNoteTab } from './useNoteTab';

/**
 * 계약 상세 page hook — fetching / 권한 / 탭 오케스트레이션 / headerActions 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `contractInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useContractDetailPage(contractId: number) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.CONTRACTS);

  const detailQuery = useGetContractQuery(contractId, { skip: !contractId });

  const paymentTab = usePaymentTab(contractId, detailQuery.data?.payments ?? []);
  const noteTab = useNoteTab(contractId, detailQuery.data?.notes ?? []);

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.CONTRACTS]),
    },
    ...(canWrite
      ? [
          {
            design: 'create' as const,
            label: '수정',
            icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.CONTRACTS]}/${contractId}/edit`),
            menuCode: MENU_CODE.CONTRACTS,
          },
        ]
      : []),
  ];

  return {
    queries: { detail: detailQuery },
    headerActions,
    tabsList: [paymentTab.tab, noteTab.tab],
    tabs: {
      payment: paymentTab.modal,
      note: noteTab.modal,
    },
  };
}

export function contractInfoFields(d: ContractDetail): HeaderDetailField[] {
  return [
    { label: '계약번호', value: d.contractNo },
    { label: '상태', value: <ContractStatusIndicator status={d.status} /> },
    { label: '고객사', value: d.customerName },
    { label: '계약자', value: d.employeeName },
    { label: '유형', value: d.categoryName },
    { label: '설비명', value: d.productModelName },
    { label: '공급사', value: d.supplierName },
    { label: '출력', value: formatOutput(d.outputValue, d.outputUnit) },
    { label: '옵션', value: d.optionText, fullWidth: true },
    { label: 'CRETOP 등급', value: d.cretopGrade },
    {
      label: '지원사업',
      value:
        d.supportProgramName || d.supportProgramStatus !== 'NONE'
          ? `${d.supportProgramName ?? ''} (${SUPPORT_PROGRAM_STATUS_LABELS[d.supportProgramStatus]})`.trim()
          : null,
    },
    { label: '초기 계약금액', value: d.initialAmount == null ? null : formatKrw(d.initialAmount) },
    { label: '최종 계약금액', value: formatKrw(d.finalAmount) },
    { label: '입금 합계', value: formatKrw(d.paidTotal) },
    {
      label: '미수금',
      value: (
        <Typography
          component="span"
          sx={{
            fontSize: 'inherit',
            fontWeight: 500,
            color: d.outstandingAmount > 0 ? 'warning.main' : 'text.disabled',
          }}
        >
          {formatKrw(d.outstandingAmount)}
        </Typography>
      ),
    },
    { label: '계약일', value: d.contractDate },
    { label: '납기일', value: d.dueDate },
    { label: '발주일', value: d.orderDate },
    { label: '입고 예정일', value: d.expectedArrivalDate },
    { label: '입고일', value: d.arrivalDate },
    { label: '설치 완료일', value: d.installedDate },
    { label: '정산 완료일', value: d.settledDate },
    { label: '물류 메모', value: d.logisticsNote, fullWidth: true },
  ];
}
