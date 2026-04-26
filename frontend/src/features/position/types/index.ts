export interface PositionSummary {
  id: number;
  code: string;
  name: string;
  rankLevel: number;
  description: string | null;
}

export interface PositionDetail {
  id: number;
  code: string;
  name: string;
  rankLevel: number;
  description: string | null;
}

export interface PositionCreateRequest {
  /** null/빈 문자열 = 채번 규칙의 inputMode 가 AUTO 또는 AUTO_OR_MANUAL 일 때 시스템이 자동 생성. */
  code: string | null;
  name: string;
  description: string | null;
}

export interface PositionUpdateRequest {
  name: string;
  description: string | null;
}

export interface PositionRankingRequest {
  /** 상위 → 하위 순. 서버가 1, 2, 3 ... 으로 일괄 갱신. */
  orderedIds: number[];
}

export interface PositionSearchParams {
  keyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type PositionListFilters = Omit<PositionSearchParams, 'page' | 'size' | 'sort'>;

export interface PositionFormValues {
  code: string;
  name: string;
  description: string;
}

export const EMPTY_POSITION_FORM: PositionFormValues = {
  code: '',
  name: '',
  description: '',
};

export function positionDetailToFormValues(d: PositionDetail): PositionFormValues {
  return {
    code: d.code,
    name: d.name,
    description: d.description ?? '',
  };
}

export function positionFormToCreateRequest(v: PositionFormValues): PositionCreateRequest {
  const trimmedCode = v.code.trim();
  const trimmedDescription = v.description.trim();
  return {
    code: trimmedCode === '' ? null : trimmedCode,
    name: v.name.trim(),
    description: trimmedDescription === '' ? null : trimmedDescription,
  };
}

export function positionFormToUpdateRequest(v: PositionFormValues): PositionUpdateRequest {
  const trimmedDescription = v.description.trim();
  return {
    name: v.name.trim(),
    description: trimmedDescription === '' ? null : trimmedDescription,
  };
}
