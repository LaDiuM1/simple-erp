import { describe, expect, it } from 'vitest';
import {
  CONTRACT_STATUS,
  EMPTY_CONTRACT_FORM,
  type ContractFormValues,
  type ContractStatus,
  type ContractDetail,
} from '@/features/contract/types';
import {
  validateContractSchedule,
  validateInstalledContractChange,
} from './contractFormValidation';

function schedule(
  status: ContractStatus,
  dates: Partial<Pick<
    ContractFormValues,
    'contractDate' | 'orderDate' | 'expectedArrivalDate' | 'arrivalDate' | 'installedDate' | 'settledDate'
  >> = {},
): ContractFormValues {
  return {
    ...EMPTY_CONTRACT_FORM,
    status,
    contractDate: '2026-01-01',
    ...dates,
  };
}

describe('validateContractSchedule', () => {
  it.each([
    [CONTRACT_STATUS.CONTRACTED, {}],
    [CONTRACT_STATUS.ORDERED, { orderDate: '2026-01-02' }],
    [CONTRACT_STATUS.ARRIVED, { orderDate: '2026-01-02', arrivalDate: '2026-01-03' }],
    [CONTRACT_STATUS.INSTALLING, { orderDate: '2026-01-02', arrivalDate: '2026-01-03' }],
    [CONTRACT_STATUS.INSTALLED, {
      orderDate: '2026-01-02', arrivalDate: '2026-01-03', installedDate: '2026-01-04',
    }],
    [CONTRACT_STATUS.SETTLED, {
      orderDate: '2026-01-02', arrivalDate: '2026-01-03',
      installedDate: '2026-01-04', settledDate: '2026-01-05',
    }],
  ] as const)('%s 상태의 정확한 실제 이정표를 허용한다', (status, dates) => {
    expect(validateContractSchedule(schedule(status, dates))).toBeNull();
  });

  it('상태보다 앞선 필수 날짜가 없거나 후속 날짜가 있으면 거부한다', () => {
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.INSTALLED, {
      orderDate: '2026-01-02',
      installedDate: '2026-01-04',
    }))).not.toBeNull();
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.ORDERED, {
      orderDate: '2026-01-02',
      arrivalDate: '2026-01-03',
    }))).not.toBeNull();
  });

  it('같은 날의 연속 이정표는 허용하고 역전된 날짜는 거부한다', () => {
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.SETTLED, {
      orderDate: '2026-01-01',
      arrivalDate: '2026-01-01',
      installedDate: '2026-01-01',
      settledDate: '2026-01-01',
    }))).toBeNull();
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.ARRIVED, {
      orderDate: '2026-01-03',
      arrivalDate: '2026-01-02',
    }))).not.toBeNull();
  });

  it('취소 상태는 날짜 존재를 강제하지 않지만 입력된 날짜의 선행 관계는 지킨다', () => {
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.CANCELED))).toBeNull();
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.CANCELED, {
      arrivalDate: '2026-01-03',
    }))).not.toBeNull();
  });

  it('실제 이정표의 미래 날짜는 상태와 순서가 맞아도 거부한다', () => {
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.ORDERED, {
      orderDate: '2026-01-06',
    }), '2026-01-05')).not.toBeNull();
    expect(validateContractSchedule(schedule(CONTRACT_STATUS.CONTRACTED, {
      expectedArrivalDate: '2026-02-01',
    }), '2026-01-05')).toBeNull();
  });
});

describe('validateInstalledContractChange', () => {
  const detail = {
    id: 1,
    contractNo: 'CT2026-001',
    customerId: 1,
    customerName: '고객사',
    employeeId: 2,
    employeeName: '담당자',
    supplierId: 7,
    supplierName: '공급사',
    productId: 3,
    productModelName: 'MODEL',
    categoryName: '분류',
    outputValue: 12,
    outputUnit: 'KW',
    optionText: null,
    initialAmount: null,
    finalAmount: 1000,
    paidTotal: 0,
    outstandingAmount: 1000,
    cretopGrade: null,
    supportProgramName: null,
    supportProgramStatus: 'NONE',
    contractDate: '2026-01-01',
    dueDate: null,
    orderDate: '2026-01-02',
    expectedArrivalDate: null,
    arrivalDate: '2026-01-03',
    installedDate: '2026-01-04',
    settledDate: null,
    logisticsNote: null,
    status: CONTRACT_STATUS.INSTALLED,
    payments: [],
    notes: [],
  } satisfies ContractDetail;

  it('설치 완료 스냅샷은 숫자 표기만 달라도 허용하고 값 변경은 거부한다', () => {
    const values = { ...schedule(CONTRACT_STATUS.INSTALLED, {
      orderDate: '2026-01-02',
      arrivalDate: '2026-01-03',
      installedDate: '2026-01-04',
    }), customerId: '1', productId: '3', outputValue: '12.0', outputUnit: 'KW' };
    expect(validateInstalledContractChange(detail, values)).toBeNull();
    expect(validateInstalledContractChange(detail, { ...values, productId: '4' })).not.toBeNull();
    expect(validateInstalledContractChange(detail, {
      ...values,
      status: CONTRACT_STATUS.CONTRACTED,
    })).not.toBeNull();
  });
});
