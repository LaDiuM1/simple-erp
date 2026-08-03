import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import {
  WARRANTY_DECISION,
  type AfterServiceFormValues,
  type WarrantyDecision,
} from '@/features/afterService/types';
import type { EquipmentReference } from '@/features/equipment/types';

const AMOUNT_RE = /^\d+$/;

export const afterServiceValidators: ValidatorMap<AfterServiceFormValues> = {
  customerId: (v) => (v === '' ? '고객사를 선택해주세요.' : null),
  receivedDate: (v) => (v === '' ? '접수일을 선택해주세요.' : null),
  billingAmount: (v) =>
    v.trim() !== '' && !AMOUNT_RE.test(v.trim()) ? '금액은 숫자만 입력해주세요.' : null,
};

export interface WarrantySuggestion {
  suggestion: WarrantyDecision;
  /** 판정 근거 안내 문구 — 판정 select 의 helperText 로 노출. */
  text: string;
}

/**
 * 설비 보증 만료일 기반 유상 / 무상 제안 — 그외 무상 AS 만료일과 접수일 비교가 기본 축이고,
 * 발진기 (레이저 소스) 부위는 별도 만료일을 함께 안내해 담당자가 수동 확정한다.
 * 설비 미연결 / 만료일 미입력이면 제안 없음.
 */
export function suggestWarrantyDecision(
  equipment: EquipmentReference | undefined,
  receivedDate: string,
): WarrantySuggestion | null {
  if (!equipment || receivedDate === '') return null;
  const generalEnd = equipment.generalWarrantyEndDate;
  if (!generalEnd) return null;

  const withinGeneral = receivedDate <= generalEnd;
  const suggestion = withinGeneral ? WARRANTY_DECISION.FREE : WARRANTY_DECISION.PAID;

  const oscillatorText = equipment.oscillatorWarrantyEndDate
    ? ` / 발진기 보증 ${equipment.oscillatorWarrantyEndDate} 까지`
    : '';
  const text = withinGeneral
    ? `설비 보증 기준 무상 제안 — 무상 AS ${generalEnd} 까지${oscillatorText}`
    : `설비 보증 기준 유상 제안 — 무상 AS ${generalEnd} 만료${oscillatorText}`;

  return { suggestion, text };
}
