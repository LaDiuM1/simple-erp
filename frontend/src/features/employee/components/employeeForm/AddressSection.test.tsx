import { describe, expect, it, vi } from 'vitest';
import { EMPTY_EMPLOYEE_FORM } from '@/features/employee/types';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import { renderWithTheme } from '@/test/renderWithTheme';
import AddressSection from './AddressSection';

const addressField = vi.hoisted(() => ({
  props: null as null | { readOnly?: boolean },
}));

vi.mock('@/shared/ui/AddressSearchField', () => ({
  default: (props: typeof addressField.props) => {
    addressField.props = props;
    return null;
  },
}));

describe('Employee AddressSection', () => {
  it('상세 화면의 읽기 전용 정책도 공용 AddressSearchField 에 전달한다', () => {
    const form = {
      values: EMPTY_EMPLOYEE_FORM,
      update: vi.fn(),
      validation: {
        onBlur: vi.fn(),
        errorMessage: vi.fn(),
        isInvalid: vi.fn(),
        validateAll: vi.fn(),
      },
    } satisfies EmployeeFormStateBase;

    renderWithTheme(<AddressSection form={form} readOnly />);

    expect(addressField.props?.readOnly).toBe(true);
  });
});
