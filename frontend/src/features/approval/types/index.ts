import type { ApprovalLineEntry } from '@/shared/ui/ApprovalLineField';
import type { AttachedFile } from '@/shared/ui/FileAttachField';

export type ApprovalStatus = 'IN_PROGRESS' | 'APPROVED' | 'REJECTED' | 'CANCELED';

export type StepStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type ApprovalDocType = 'GENERAL' | 'EXPENSE' | 'LEAVE';

/** 결재함 구분 — BE 목록 검색의 필수 조건. null 필터 ('전체') 는 api 레이어가 INVOLVED 로 매핑해 전송. */
export type ApprovalBox = 'DRAFTED' | 'PENDING' | 'PROCESSED' | 'INVOLVED';

export const APPROVAL_STATUS_LABELS: Record<ApprovalStatus, string> = {
  IN_PROGRESS: '진행 중',
  APPROVED: '승인',
  REJECTED: '반려',
  CANCELED: '상신 취소',
};

export const STEP_STATUS_LABELS: Record<StepStatus, string> = {
  PENDING: '대기',
  APPROVED: '승인',
  REJECTED: '반려',
};

export const APPROVAL_DOC_TYPE_LABELS: Record<ApprovalDocType, string> = {
  GENERAL: '일반 기안',
  EXPENSE: '경비 청구',
  LEAVE: '휴가 신청',
};

export const APPROVAL_STATUS_OPTIONS: { value: ApprovalStatus; label: string }[] = [
  { value: 'IN_PROGRESS', label: '진행 중' },
  { value: 'APPROVED', label: '승인' },
  { value: 'REJECTED', label: '반려' },
  { value: 'CANCELED', label: '상신 취소' },
];

export const APPROVAL_DOC_TYPE_OPTIONS: { value: ApprovalDocType; label: string }[] = [
  { value: 'GENERAL', label: '일반 기안' },
  { value: 'EXPENSE', label: '경비 청구' },
  { value: 'LEAVE', label: '휴가 신청' },
];

/** 결재함 필터 옵션 — '전체' (INVOLVED) 는 FilterSelect 의 기본 전체 옵션 (null) 이 흡수. */
export const APPROVAL_BOX_OPTIONS: { value: ApprovalBox; label: string }[] = [
  { value: 'DRAFTED', label: '내 기안' },
  { value: 'PENDING', label: '결재 대기' },
  { value: 'PROCESSED', label: '처리함' },
];

export interface ApprovalSummary {
  id: number;
  docType: ApprovalDocType;
  title: string;
  drafterName: string;
  status: ApprovalStatus;
  createdAt: string;
  currentStepOrder: number;
  totalSteps: number;
}

export interface ApprovalStep {
  stepOrder: number;
  approverId: number;
  approverName: string;
  status: StepStatus;
  comment: string | null;
  decidedAt: string | null;
}

export interface ApprovalAttachment {
  fileId: number;
  name: string;
  size: number;
}

/** 결재 문서 상세 — myTurn / cancelable 은 BE 가 현재 사용자 관점으로 계산한 플래그. */
export interface ApprovalDetail {
  id: number;
  docType: ApprovalDocType;
  title: string;
  content: string | null;
  drafterId: number;
  drafterName: string;
  refId: number | null;
  status: ApprovalStatus;
  currentStepOrder: number;
  createdAt: string;
  steps: ApprovalStep[];
  attachments: ApprovalAttachment[];
  myTurn: boolean;
  cancelable: boolean;
}

export interface ApprovalCreateRequest {
  title: string;
  content: string | null;
  approverIds: number[];
  attachmentFileIds: number[];
}

/** 승인 / 반려 결정 요청 — 의견은 선택. */
export interface ApprovalDecisionRequest {
  comment: string | null;
}

export interface ApprovalSearchParams {
  box?: ApprovalBox | null;
  status?: ApprovalStatus | null;
  docType?: ApprovalDocType | null;
  keyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

export type ApprovalListFilters = Omit<ApprovalSearchParams, 'page' | 'size' | 'sort'>;

export interface ApprovalFormValues {
  title: string;
  content: string;
  line: ApprovalLineEntry[];
  attachments: AttachedFile[];
}

export const EMPTY_APPROVAL_FORM: ApprovalFormValues = {
  title: '',
  content: '',
  line: [],
  attachments: [],
};

export function approvalFormToCreateRequest(v: ApprovalFormValues): ApprovalCreateRequest {
  return {
    title: v.title.trim(),
    content: emptyToNull(v.content),
    approverIds: v.line.map((entry) => entry.employeeId),
    attachmentFileIds: v.attachments.map((file) => file.fileId),
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
