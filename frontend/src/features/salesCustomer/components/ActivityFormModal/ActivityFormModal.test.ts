import { describe, expect, it } from 'vitest';
import { clearActivityCustomerContact } from './activityFormState';

describe('clearActivityCustomerContact', () => {
  it('고객 변경 시 이전 담당자를 해제하고 작성 중인 활동 내용은 보존한다', () => {
    const result = clearActivityCustomerContact({
      type: 'VISIT',
      activityDate: '2026-08-11T10:00',
      subject: '상담',
      content: '작성 중인 내용',
      ourEmployeeId: '1',
      ourEmployeeName: '담당자',
      customerContactId: '9',
      customerContactSelectedName: '이전 고객 담당자',
    });

    expect(result).toEqual(expect.objectContaining({
      subject: '상담',
      content: '작성 중인 내용',
      customerContactId: '',
      customerContactSelectedName: '',
    }));
  });
});
