import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CommonSearchModal from './CommonSearchModal';

const mockUseMediaQuery = vi.fn(() => false);
vi.mock('@mui/material/useMediaQuery', () => ({ default: () => mockUseMediaQuery() }));

vi.stubGlobal('ResizeObserver', class {
  observe() {}
  unobserve() {}
  disconnect() {}
});

interface Row {
  id: number;
  name: string;
}

interface Filters {
  customerId?: number | null;
}

const page = {
  content: [
    { id: 1, name: '선택 후보' },
    { id: 2, name: '다른 후보' },
  ],
  page: 0,
  size: 10,
  totalElements: 2,
  totalPages: 1,
  hasNext: false,
};

const api = {
  useList: () => ({
    data: page,
    isFetching: false,
    isError: false,
    refetch: vi.fn(),
  }),
  rowKey: (row: Row) => row.id,
  rowLabel: (row: Row) => row.name,
};

describe('CommonSearchModal scope', () => {
  beforeEach(() => {
    mockUseMediaQuery.mockReturnValue(false);
  });

  it('열린 상태에서 scope가 바뀌면 이전 선택을 폐기한다', () => {
    const props = {
      open: true,
      onClose: vi.fn(),
      title: '검색',
      api,
      column: [{ key: 'name', label: '이름' }],
      onSelect: vi.fn(),
    };
    const { rerender } = render(
      <CommonSearchModal<Row, Filters>
        {...props}
        scopeKey="customer:1"
        fixedQueryParams={{ customerId: 1 }}
      />,
    );

    fireEvent.click(screen.getByText('선택 후보'));
    expect(screen.getByRole('radio', { name: '선택 후보' })).toBeChecked();

    rerender(
      <CommonSearchModal<Row, Filters>
        {...props}
        scopeKey="customer:2"
        fixedQueryParams={{ customerId: 2 }}
      />,
    );

    expect(screen.getByRole('radio', { name: '선택 후보' })).not.toBeChecked();
  });

  it('후보명으로 라디오를 식별하고 키보드로 단일 선택한다', async () => {
    const user = userEvent.setup();
    render(
      <CommonSearchModal<Row, Filters>
        open
        onClose={vi.fn()}
        title="검색"
        api={api}
        column={[{ key: 'name', label: '이름' }]}
        onSelect={vi.fn()}
      />,
    );

    const first = screen.getByRole('radio', { name: '선택 후보' });
    const second = screen.getByRole('radio', { name: '다른 후보' });

    expect(first).toHaveAttribute('name');
    expect(second).toHaveAttribute('name', first.getAttribute('name'));

    first.focus();
    await user.keyboard(' ');
    expect(first).toBeChecked();
    expect(second).not.toBeChecked();

    second.focus();
    await user.keyboard(' ');
    expect(first).not.toBeChecked();
    expect(second).toBeChecked();
  });

  it('선택 트레이 모드에서도 행을 식별하고 키보드로 누적 선택한다', async () => {
    const user = userEvent.setup();
    render(
      <CommonSearchModal<Row, Filters>
        open
        onClose={vi.fn()}
        title="검색"
        api={api}
        column={[{ key: 'name', label: '이름' }]}
        onSelect={vi.fn()}
        multiple
        selectionStyle="tray"
      />,
    );

    const first = screen.getByRole('checkbox', { name: '선택 후보' });
    const second = screen.getByRole('checkbox', { name: '다른 후보' });
    expect(first.closest('tr')).not.toHaveAttribute('role');
    expect(second.closest('tr')).not.toHaveAttribute('role');

    first.focus();
    await user.keyboard(' ');
    expect(first).toBeChecked();

    second.focus();
    await user.keyboard('{Enter}');
    expect(first).toBeChecked();
    expect(second).toBeChecked();

    await user.keyboard(' ');
    expect(second).not.toBeChecked();
  });

  it('모바일 선택 트레이도 네이티브 컨트롤로 키보드 선택한다', async () => {
    mockUseMediaQuery.mockReturnValue(true);
    const user = userEvent.setup();
    const { container } = render(
      <CommonSearchModal<Row, Filters>
        open
        onClose={vi.fn()}
        title="검색"
        api={api}
        column={[{ key: 'name', label: '이름', mobilePrimary: true }]}
        onSelect={vi.fn()}
        multiple
        selectionStyle="tray"
      />,
    );

    const first = screen.getByRole('checkbox', { name: '선택 후보' });
    expect(first.closest('tr')).toBeNull();
    expect(first.closest('[data-tray-selection]')).toBeInTheDocument();

    first.focus();
    await user.keyboard(' ');
    expect(first).toBeChecked();

    await user.keyboard('{Enter}');
    expect(first).not.toBeChecked();
    expect(container.querySelector('[role="checkbox"]')).toBeNull();
  });
});
