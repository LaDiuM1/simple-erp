export type AcquisitionSourceType = 'EXHIBITION' | 'REFERRAL' | 'WEB' | 'OTHER';

export const ACQUISITION_SOURCE_TYPE_LABELS: Record<AcquisitionSourceType, string> = {
  EXHIBITION: '전시회',
  REFERRAL: '소개',
  WEB: '웹·인터넷',
  OTHER: '기타',
};

export const ACQUISITION_SOURCE_TYPE_OPTIONS: { value: AcquisitionSourceType; label: string }[] = [
  { value: 'EXHIBITION', label: '전시회' },
  { value: 'REFERRAL', label: '소개' },
  { value: 'WEB', label: '웹·인터넷' },
  { value: 'OTHER', label: '기타' },
];

export interface AcquisitionSourceInfo {
  id: number;
  name: string;
  type: AcquisitionSourceType;
  description: string | null;
}

export interface AcquisitionSourceCreateRequest {
  name: string;
  type: AcquisitionSourceType;
  description: string | null;
}
