import type { ApprovalLineEntry } from '@/shared/ui/ApprovalLineField';
import type { AttachedFile } from '@/shared/ui/FileAttachField';
import { todayIsoDate } from '@/shared/utils/date';

export type ExpenseCategory = 'TRANSPORT' | 'MEAL' | 'LODGING' | 'SUPPLIES' | 'ETC';

export const EXPENSE_CATEGORY_LABELS: Record<ExpenseCategory, string> = {
  TRANSPORT: '교통비',
  MEAL: '식대',
  LODGING: '숙박비',
  SUPPLIES: '소모품비',
  ETC: '기타',
};

export const EXPENSE_CATEGORY_OPTIONS: { value: ExpenseCategory; label: string }[] = [
  { value: 'TRANSPORT', label: '교통비' },
  { value: 'MEAL', label: '식대' },
  { value: 'LODGING', label: '숙박비' },
  { value: 'SUPPLIES', label: '소모품비' },
  { value: 'ETC', label: '기타' },
];

export type ExpenseStatus = 'IN_PROGRESS' | 'APPROVED' | 'REJECTED';

export const EXPENSE_STATUS_LABELS: Record<ExpenseStatus, string> = {
  IN_PROGRESS: '결재 중',
  APPROVED: '승인',
  REJECTED: '반려',
};

export const EXPENSE_STATUS_OPTIONS: { value: ExpenseStatus; label: string }[] = [
  { value: 'IN_PROGRESS', label: '결재 중' },
  { value: 'APPROVED', label: '승인' },
  { value: 'REJECTED', label: '반려' },
];

/** 목록 조회 범위 — 기본 MINE (본인 청구), ALL 은 EXPENSES 쓰기 권한자 (정산 담당) 전용. */
export type ExpenseSearchScope = 'MINE' | 'ALL';

export const EXPENSE_SCOPE_OPTIONS: { value: ExpenseSearchScope; label: string }[] = [
  { value: 'MINE', label: '내 경비' },
  { value: 'ALL', label: '전체 경비' },
];

export interface ExpenseSummary {
  id: number;
  title: string;
  claimantName: string;
  totalAmount: number;
  status: ExpenseStatus;
  approvalDocumentId: number | null;
  createdAt: string;
  itemCount: number;
}

export interface ExpenseItem {
  id: number;
  expenseDate: string;
  category: ExpenseCategory;
  amount: number;
  description: string | null;
  receiptFileId: number | null;
  receiptFileName: string | null;
}

export interface ExpenseDetail {
  id: number;
  title: string;
  totalAmount: number;
  status: ExpenseStatus;
  claimantName: string;
  approvalDocumentId: number | null;
  createdAt: string;
  items: ExpenseItem[];
}

export interface ExpenseItemCreateRequest {
  expenseDate: string;
  category: ExpenseCategory;
  amount: number;
  description?: string | null;
  receiptFileId?: number | null;
}

/** 생성 = 즉시 상신 — approverIds 순서가 결재 순서. */
export interface ExpenseCreateRequest {
  title: string;
  items: ExpenseItemCreateRequest[];
  approverIds: number[];
}

/** startDate / endDate 는 청구 생성일 (createdAt) 기간 — 'YYYY-MM-DD'. scope null 은 api 레이어가 MINE 으로 매핑. */
export interface ExpenseSearchParams {
  scope?: ExpenseSearchScope | null;
  status?: ExpenseStatus | null;
  startDate?: string | null;
  endDate?: string | null;
  keyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

export type ExpenseListFilters = Omit<ExpenseSearchParams, 'page' | 'size' | 'sort'>;

export interface ExpenseItemFormValues {
  /** 클라이언트 행 식별자 — react key / 행 추가·제거용. 서버 전송 안 함. */
  rowId: number;
  expenseDate: string;
  category: ExpenseCategory;
  /** 입력 문자열 그대로 유지 — submit 시 Number 변환. */
  amount: string;
  description: string;
  /** FileAttachField single 모드 — 0 또는 1개. */
  receipt: AttachedFile[];
}

export interface ExpenseFormValues {
  title: string;
  items: ExpenseItemFormValues[];
  approvalLine: ApprovalLineEntry[];
}

export function createEmptyExpenseItem(rowId: number): ExpenseItemFormValues {
  return {
    rowId,
    expenseDate: todayIsoDate(),
    category: 'TRANSPORT',
    amount: '',
    description: '',
    receipt: [],
  };
}

/** 항목 배열은 rowId 가 필요해 상수 대신 팩토리 — 초기 폼은 빈 항목 1행으로 시작. */
export function emptyExpenseForm(): ExpenseFormValues {
  return {
    title: '',
    items: [createEmptyExpenseItem(0)],
    approvalLine: [],
  };
}

export function expenseFormToCreateRequest(v: ExpenseFormValues): ExpenseCreateRequest {
  return {
    title: v.title.trim(),
    items: v.items.map((item) => ({
      expenseDate: item.expenseDate,
      category: item.category,
      amount: Number(item.amount),
      description: emptyToNull(item.description),
      receiptFileId: item.receipt[0]?.fileId ?? null,
    })),
    approverIds: v.approvalLine.map((entry) => entry.employeeId),
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
