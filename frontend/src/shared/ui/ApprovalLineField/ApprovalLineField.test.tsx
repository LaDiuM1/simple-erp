import { act } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import ApprovalLineField from './ApprovalLineField';

const employeeField = vi.hoisted(() => ({
  props: null as null | {
    excludeId?: number;
    onChange: (id: string, name: string) => void;
  },
}));

vi.mock('@/features/employee/api/employeeApi', () => ({
  useGetMyProfileQuery: () => ({ data: { id: 7, name: '기안자' } }),
}));

vi.mock('@/features/employee/components/EmployeeSelectField/EmployeeSelectField', () => ({
  default: (props: typeof employeeField.props) => {
    employeeField.props = props;
    return null;
  },
}));

describe('ApprovalLineField', () => {
  it('기안자를 검색 후보에서 제외하고 선택 직전에도 차단한다', () => {
    const onChange = vi.fn();
    renderWithTheme(<ApprovalLineField value={[]} onChange={onChange} />);

    expect(employeeField.props?.excludeId).toBe(7);
    act(() => employeeField.props?.onChange('7', '기안자'));
    expect(onChange).not.toHaveBeenCalled();

    act(() => employeeField.props?.onChange('8', '결재자'));
    expect(onChange).toHaveBeenCalledWith([{ employeeId: 8, name: '결재자' }]);
  });
});
