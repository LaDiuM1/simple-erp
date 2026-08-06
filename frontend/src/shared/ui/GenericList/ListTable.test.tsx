import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterAll, beforeAll, describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import ListTable from './ListTable';

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ canWrite: false }),
}));

beforeAll(() => {
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      disconnect() {}
    },
  );
});

afterAll(() => vi.unstubAllGlobals());

describe('ListTable', () => {
  it('클릭 가능한 데스크톱 행을 키보드로 연다', async () => {
    const user = userEvent.setup();
    const onRowClick = vi.fn();

    renderWithTheme(
      <ListTable
        menuCode="CUSTOMERS"
        columns={[
          { key: 'category', label: '분류', width: 100 },
          { key: 'name', label: '고객사', flex: 1, mobilePrimary: true },
        ]}
        rows={[{ id: 7, category: '주요고객', name: '온결산업' }]}
        rowKey={(row) => row.id}
        page={0}
        pageSize={10}
        sort={{ key: 'name', direction: 'asc' }}
        onSortChange={vi.fn()}
        filters={{}}
        onRowClick={onRowClick}
      />,
    );

    const openButton = screen.getByRole('button', { name: '온결산업' });
    openButton.focus();
    await user.keyboard('{Enter}');
    await user.keyboard(' ');

    expect(onRowClick).toHaveBeenCalledTimes(2);
    expect(onRowClick).toHaveBeenLastCalledWith({
      id: 7,
      category: '주요고객',
      name: '온결산업',
    });
  });
});
