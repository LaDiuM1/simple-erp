import { describe, expect, it, vi } from 'vitest';
import { EMPTY_CUSTOMER_FORM } from '@/features/customer/types';
import type { CustomerFormStateBase } from '@/features/customer/hooks/customerFormState';
import { renderWithTheme } from '@/test/renderWithTheme';
import AddressSection from './AddressSection';

const addressField = vi.hoisted(() => ({
  props: null as null | {
    zipCode: string;
    roadAddress: string;
    onZipCodeChange: (value: string) => void;
    onRoadAddressChange: (value: string) => void;
    readOnly?: boolean;
  },
}));

vi.mock('@/shared/ui/AddressSearchField', () => ({
  default: (props: typeof addressField.props) => {
    addressField.props = props;
    return null;
  },
}));

describe('Customer AddressSection', () => {
  it('등록·수정 주소 입력을 공용 AddressSearchField 경계로 위임한다', () => {
    const update = vi.fn();
    const form = {
      values: EMPTY_CUSTOMER_FORM,
      update,
      validation: {
        onBlur: vi.fn(),
        errorMessage: vi.fn(),
        isInvalid: vi.fn(),
        validateAll: vi.fn(),
      },
      bizRegNoStatus: 'idle',
    } satisfies CustomerFormStateBase;

    renderWithTheme(<AddressSection form={form} />);
    addressField.props?.onZipCodeChange('04524');
    addressField.props?.onRoadAddressChange('서울시 중구 세종대로 110');

    expect(update).toHaveBeenCalledWith('zipCode', '04524');
    expect(update).toHaveBeenCalledWith('roadAddress', '서울시 중구 세종대로 110');
    expect(addressField.props?.readOnly).toBe(false);
  });
});
