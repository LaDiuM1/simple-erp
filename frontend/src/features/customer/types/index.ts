export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export const CUSTOMER_STATUS_LABELS: Record<CustomerStatus, string> = {
  ACTIVE: '거래중',
  INACTIVE: '비거래',
  SUSPENDED: '거래중지',
};

export const CUSTOMER_STATUS_OPTIONS: { value: CustomerStatus; label: string }[] = [
  { value: 'ACTIVE', label: '거래중' },
  { value: 'INACTIVE', label: '비거래' },
  { value: 'SUSPENDED', label: '거래중지' },
];

export type CustomerType = 'POTENTIAL' | 'GENERAL' | 'KEY_ACCOUNT' | 'PARTNER';

export const CUSTOMER_TYPE_LABELS: Record<CustomerType, string> = {
  POTENTIAL: '잠재고객',
  GENERAL: '일반고객',
  KEY_ACCOUNT: '주요고객',
  PARTNER: '파트너',
};

export const CUSTOMER_TYPE_OPTIONS: { value: CustomerType; label: string }[] = [
  { value: 'POTENTIAL', label: '잠재고객' },
  { value: 'GENERAL', label: '일반고객' },
  { value: 'KEY_ACCOUNT', label: '주요고객' },
  { value: 'PARTNER', label: '파트너' },
];

export interface CustomerSummary {
  id: number;
  code: string;
  name: string;
  bizRegNo: string | null;
  representative: string | null;
  phone: string | null;
  email: string | null;
  roadAddress: string | null;
  type: CustomerType;
  status: CustomerStatus;
  tradeStartDate: string | null;
}

export interface CustomerDetail {
  id: number;
  code: string;
  name: string;
  nameEn: string | null;
  bizRegNo: string | null;
  corpRegNo: string | null;
  representative: string | null;
  bizType: string | null;
  bizItem: string | null;
  phone: string | null;
  fax: string | null;
  email: string | null;
  website: string | null;
  zipCode: string | null;
  roadAddress: string | null;
  detailAddress: string | null;
  type: CustomerType;
  status: CustomerStatus;
  tradeStartDate: string | null;
  note: string | null;
}

export interface CustomerCreateRequest {
  /** null/빈 문자열 = 채번 규칙의 inputMode 가 AUTO 또는 AUTO_OR_MANUAL 일 때 시스템이 자동 생성. */
  code: string | null;
  name: string;
  nameEn?: string | null;
  bizRegNo?: string | null;
  corpRegNo?: string | null;
  representative?: string | null;
  bizType?: string | null;
  bizItem?: string | null;
  phone?: string | null;
  fax?: string | null;
  email?: string | null;
  website?: string | null;
  zipCode?: string | null;
  roadAddress?: string | null;
  detailAddress?: string | null;
  type: CustomerType;
  status: CustomerStatus;
  tradeStartDate?: string | null;
  note?: string | null;
}

export interface CustomerUpdateRequest {
  name: string;
  nameEn?: string | null;
  bizRegNo?: string | null;
  corpRegNo?: string | null;
  representative?: string | null;
  bizType?: string | null;
  bizItem?: string | null;
  phone?: string | null;
  fax?: string | null;
  email?: string | null;
  website?: string | null;
  zipCode?: string | null;
  roadAddress?: string | null;
  detailAddress?: string | null;
  type: CustomerType;
  status: CustomerStatus;
  tradeStartDate?: string | null;
  note?: string | null;
}

export interface CustomerSearchParams {
  keyword?: string | null;
  addressKeyword?: string | null;
  phoneKeyword?: string | null;
  type?: CustomerType | null;
  status?: CustomerStatus | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type CustomerListFilters = Omit<CustomerSearchParams, 'page' | 'size' | 'sort'>;

export interface CustomerFormValues {
  code: string;
  name: string;
  nameEn: string;
  bizRegNo: string;
  corpRegNo: string;
  representative: string;
  bizType: string;
  bizItem: string;
  phone: string;
  fax: string;
  email: string;
  website: string;
  zipCode: string;
  roadAddress: string;
  detailAddress: string;
  type: CustomerType;
  status: CustomerStatus;
  tradeStartDate: string;
  note: string;
}

export const EMPTY_CUSTOMER_FORM: CustomerFormValues = {
  code: '',
  name: '',
  nameEn: '',
  bizRegNo: '',
  corpRegNo: '',
  representative: '',
  bizType: '',
  bizItem: '',
  phone: '',
  fax: '',
  email: '',
  website: '',
  zipCode: '',
  roadAddress: '',
  detailAddress: '',
  type: 'GENERAL',
  status: 'ACTIVE',
  tradeStartDate: '',
  note: '',
};

export function customerDetailToFormValues(d: CustomerDetail): CustomerFormValues {
  return {
    code: d.code,
    name: d.name,
    nameEn: d.nameEn ?? '',
    bizRegNo: d.bizRegNo ?? '',
    corpRegNo: d.corpRegNo ?? '',
    representative: d.representative ?? '',
    bizType: d.bizType ?? '',
    bizItem: d.bizItem ?? '',
    phone: d.phone ?? '',
    fax: d.fax ?? '',
    email: d.email ?? '',
    website: d.website ?? '',
    zipCode: d.zipCode ?? '',
    roadAddress: d.roadAddress ?? '',
    detailAddress: d.detailAddress ?? '',
    type: d.type,
    status: d.status,
    tradeStartDate: d.tradeStartDate ?? '',
    note: d.note ?? '',
  };
}

export function customerFormToCreateRequest(v: CustomerFormValues): CustomerCreateRequest {
  const trimmedCode = v.code.trim();
  return {
    code: trimmedCode === '' ? null : trimmedCode,
    name: v.name.trim(),
    nameEn: emptyToNull(v.nameEn),
    bizRegNo: emptyToNull(v.bizRegNo),
    corpRegNo: emptyToNull(v.corpRegNo),
    representative: emptyToNull(v.representative),
    bizType: emptyToNull(v.bizType),
    bizItem: emptyToNull(v.bizItem),
    phone: emptyToNull(v.phone),
    fax: emptyToNull(v.fax),
    email: emptyToNull(v.email),
    website: emptyToNull(v.website),
    zipCode: emptyToNull(v.zipCode),
    roadAddress: emptyToNull(v.roadAddress),
    detailAddress: emptyToNull(v.detailAddress),
    type: v.type,
    status: v.status,
    tradeStartDate: emptyToNull(v.tradeStartDate),
    note: emptyToNull(v.note),
  };
}

export function customerFormToUpdateRequest(v: CustomerFormValues): CustomerUpdateRequest {
  return {
    name: v.name.trim(),
    nameEn: emptyToNull(v.nameEn),
    bizRegNo: emptyToNull(v.bizRegNo),
    corpRegNo: emptyToNull(v.corpRegNo),
    representative: emptyToNull(v.representative),
    bizType: emptyToNull(v.bizType),
    bizItem: emptyToNull(v.bizItem),
    phone: emptyToNull(v.phone),
    fax: emptyToNull(v.fax),
    email: emptyToNull(v.email),
    website: emptyToNull(v.website),
    zipCode: emptyToNull(v.zipCode),
    roadAddress: emptyToNull(v.roadAddress),
    detailAddress: emptyToNull(v.detailAddress),
    type: v.type,
    status: v.status,
    tradeStartDate: emptyToNull(v.tradeStartDate),
    note: emptyToNull(v.note),
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
