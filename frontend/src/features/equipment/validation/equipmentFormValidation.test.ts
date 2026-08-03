import { describe, expect, it } from 'vitest';
import {
  EMPTY_EQUIPMENT_FORM,
  type EquipmentDetail,
} from '@/features/equipment/types';
import { validateContractLinkedEquipmentChange } from './equipmentFormValidation';

const detail = {
  id: 1,
  customerId: 1,
  customerName: '고객사',
  contractId: 10,
  contractNo: 'CT2026-001',
  supplierId: 7,
  supplierName: '공급사',
  productId: 3,
  productModelName: 'MODEL',
  categoryName: '분류',
  outputValue: 12,
  outputUnit: 'KW',
  serialNo: null,
  installAddress: null,
  installedDate: '2026-01-04',
  confirmedDate: null,
  warrantyStartDate: null,
  oscillatorWarrantyMonths: null,
  generalWarrantyMonths: null,
  oscillatorWarrantyEndDate: null,
  generalWarrantyEndDate: null,
  warrantyInsurance: false,
  note: null,
} satisfies EquipmentDetail;

describe('validateContractLinkedEquipmentChange', () => {
  it('계약 스냅샷의 숫자 표기 차이는 허용하고 실제 변경은 거부한다', () => {
    const values = {
      ...EMPTY_EQUIPMENT_FORM,
      customerId: '1',
      productId: '3',
      outputValue: '12.0',
      outputUnit: 'KW',
      installedDate: '2026-01-04',
    };
    expect(validateContractLinkedEquipmentChange(detail, values)).toBeNull();
    expect(validateContractLinkedEquipmentChange(detail, {
      ...values,
      installedDate: '2026-01-05',
    })).not.toBeNull();
  });
});
