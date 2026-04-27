import type { AcquisitionSourceInfo } from '@/features/acquisitionSource/types';

export type DepartureType = 'JOB_CHANGE' | 'RETIREMENT' | 'OTHER';

export const DEPARTURE_TYPE_LABELS: Record<DepartureType, string> = {
  JOB_CHANGE: '이직',
  RETIREMENT: '퇴직',
  OTHER: '기타',
};

export const DEPARTURE_TYPE_OPTIONS: { value: DepartureType; label: string }[] = [
  { value: 'JOB_CHANGE', label: '이직' },
  { value: 'RETIREMENT', label: '퇴직' },
  { value: 'OTHER', label: '기타' },
];

export interface SalesContactSummary {
  id: number;
  name: string;
  mobilePhone: string | null;
  email: string | null;
  currentCompanyName: string | null;
  currentPosition: string | null;
  currentDepartment: string | null;
  metAt: string | null;
  sources: AcquisitionSourceInfo[];
}

export interface SalesContactEmployment {
  id: number;
  contactId: number;
  contactName: string | null;
  customerId: number | null;
  customerName: string | null;
  externalCompanyName: string | null;
  position: string | null;
  department: string | null;
  startDate: string;
  endDate: string | null;
  departureType: DepartureType | null;
  departureNote: string | null;
  active: boolean;
}

export interface SalesContactDetail {
  id: number;
  name: string;
  nameEn: string | null;
  mobilePhone: string | null;
  officePhone: string | null;
  email: string | null;
  personalEmail: string | null;
  metAt: string | null;
  note: string | null;
  sources: AcquisitionSourceInfo[];
  employments: SalesContactEmployment[];
}

export interface SalesContactCreateRequest {
  name: string;
  nameEn?: string | null;
  mobilePhone?: string | null;
  officePhone?: string | null;
  email?: string | null;
  personalEmail?: string | null;
  metAt?: string | null;
  sourceIds?: number[] | null;
  note?: string | null;
}

export type SalesContactUpdateRequest = SalesContactCreateRequest;

export interface SalesContactSearchParams {
  nameKeyword?: string | null;
  emailKeyword?: string | null;
  phoneKeyword?: string | null;
  sourceIds?: number[] | null;
  page: number;
  size?: number;
  sort?: string;
}

export type SalesContactListFilters = Omit<SalesContactSearchParams, 'page' | 'size' | 'sort'>;

export interface SalesContactEmploymentCreateRequest {
  customerId?: number | null;
  externalCompanyName?: string | null;
  position?: string | null;
  department?: string | null;
  startDate: string;
}

export type SalesContactEmploymentUpdateRequest = SalesContactEmploymentCreateRequest;

export interface SalesContactEmploymentTerminateRequest {
  endDate: string;
  departureType: DepartureType;
  departureNote?: string | null;
}

export interface SalesContactFormValues {
  name: string;
  nameEn: string;
  mobilePhone: string;
  officePhone: string;
  email: string;
  personalEmail: string;
  metAt: string;
  sourceIds: number[];
  note: string;
}

export const EMPTY_SALES_CONTACT_FORM: SalesContactFormValues = {
  name: '',
  nameEn: '',
  mobilePhone: '',
  officePhone: '',
  email: '',
  personalEmail: '',
  metAt: '',
  sourceIds: [],
  note: '',
};

export function salesContactDetailToFormValues(d: SalesContactDetail): SalesContactFormValues {
  return {
    name: d.name,
    nameEn: d.nameEn ?? '',
    mobilePhone: d.mobilePhone ?? '',
    officePhone: d.officePhone ?? '',
    email: d.email ?? '',
    personalEmail: d.personalEmail ?? '',
    metAt: d.metAt ?? '',
    sourceIds: d.sources.map((s) => s.id),
    note: d.note ?? '',
  };
}

export function salesContactFormToCreateRequest(v: SalesContactFormValues): SalesContactCreateRequest {
  return {
    name: v.name.trim(),
    nameEn: emptyToNull(v.nameEn),
    mobilePhone: emptyToNull(v.mobilePhone),
    officePhone: emptyToNull(v.officePhone),
    email: emptyToNull(v.email),
    personalEmail: emptyToNull(v.personalEmail),
    metAt: emptyToNull(v.metAt),
    sourceIds: v.sourceIds,
    note: emptyToNull(v.note),
  };
}

export function salesContactFormToUpdateRequest(v: SalesContactFormValues): SalesContactUpdateRequest {
  return salesContactFormToCreateRequest(v);
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
