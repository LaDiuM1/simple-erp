import { screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import AssignmentTabModals, { type AssignmentTabModalProps } from './AssignmentTabModals';

vi.mock('@/features/salesCustomer/components/AssignmentFormModal/AssignmentFormModal', () => ({
  default: ({
    excludeEmployeeIds,
  }: {
    excludeEmployeeIds?: number[];
  }) => (
    <div data-testid={excludeEmployeeIds ? 'create-assignment' : 'edit-assignment'}>
      {excludeEmployeeIds?.join(',') ?? ''}
    </div>
  ),
}));

vi.mock('@/features/salesCustomer/components/AssignmentTerminateModal/AssignmentTerminateModal', () => ({
  default: () => null,
}));

describe('AssignmentTabModals', () => {
  it('신규 담당자 선택에 활성 배정 직원 제외 id 를 전달한다', () => {
    const modal: AssignmentTabModalProps = {
      customerId: 48,
      excludedEmployeeIds: [2, 5],
      creating: true,
      editing: null,
      terminating: null,
      deletingTarget: null,
      isDeleting: false,
      onCloseCreate: vi.fn(),
      onCloseEdit: vi.fn(),
      onCloseTerminate: vi.fn(),
      onCancelDelete: vi.fn(),
      onConfirmDelete: vi.fn(),
    };

    renderWithTheme(<AssignmentTabModals modal={modal} />);

    expect(screen.getByTestId('create-assignment')).toHaveTextContent('2,5');
  });
});
