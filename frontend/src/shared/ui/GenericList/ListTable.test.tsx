import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import ListTable from './ListTable';

const mocks = vi.hoisted(() => ({ canWrite: false, isMobile: false }));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ canWrite: mocks.canWrite }),
}));
vi.mock('@mui/material/useMediaQuery', () => ({ default: () => mocks.isMobile }));

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
  beforeEach(() => {
    mocks.canWrite = false;
    mocks.isMobile = false;
  });

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

  it('요청 전환 중에는 이전 행의 정렬과 열기 동작을 차단한다', async () => {
    const user = userEvent.setup();
    const onRowClick = vi.fn();
    const onSortChange = vi.fn();

    renderWithTheme(
      <ListTable
        menuCode="CUSTOMERS"
        columns={[{ key: 'name', label: '고객사', sortable: true, sortDirection: 'asc' }]}
        rows={[{ id: 7, name: '이전 고객사' }]}
        rowKey={(row) => row.id}
        page={0}
        pageSize={10}
        sort={{ key: 'name', direction: 'asc' }}
        onSortChange={onSortChange}
        filters={{}}
        onRowClick={onRowClick}
        interactionBlocked
      />,
    );

    expect(screen.queryByRole('button', { name: '이전 고객사' })).not.toBeInTheDocument();
    await user.click(screen.getByText('이전 고객사'));
    expect(screen.getByRole('button', { name: /고객사/ })).toHaveStyle({ pointerEvents: 'none' });

    expect(onRowClick).not.toHaveBeenCalled();
    expect(onSortChange).not.toHaveBeenCalled();
  });

  it('차단 오류에서도 열 머리글을 유지하고 다시 조회할 수 있다', async () => {
    const user = userEvent.setup();
    const onRetry = vi.fn();

    renderWithTheme(
      <ListTable
        menuCode="CUSTOMERS"
        columns={[{ key: 'name', label: '고객사' }]}
        rows={[] as { id: number; name: string }[]}
        rowKey={(row) => row.id}
        page={2}
        pageSize={10}
        sort={{ key: 'name', direction: 'asc' }}
        onSortChange={vi.fn()}
        filters={{}}
        interactionBlocked
        errorMessage="목록 조회 실패"
        onRetry={onRetry}
      />,
    );

    expect(screen.getByRole('columnheader', { name: '고객사' })).toBeInTheDocument();
    expect(screen.getByText('목록 조회 실패')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(onRetry).toHaveBeenCalledOnce();
  });

  it('열린 단건 삭제 확인도 요청 전환 시 실행하지 않는다', async () => {
    mocks.canWrite = true;
    mocks.isMobile = true;
    const user = userEvent.setup();
    const onDelete = vi.fn();
    const baseProps = {
      menuCode: 'CUSTOMERS',
      columns: [{ key: 'name', label: '고객사', mobilePrimary: true }],
      rows: [{ id: 7, name: '이전 고객사' }],
      rowKey: (row: { id: number; name: string }) => row.id,
      page: 0,
      pageSize: 10,
      sort: { key: 'name', direction: 'asc' as const },
      onSortChange: vi.fn(),
      filters: {},
      onDelete,
    };
    const { rerender } = renderWithTheme(<ListTable {...baseProps} />);

    await user.click(screen.getByRole('button', { name: '삭제' }));
    rerender(<ListTable {...baseProps} interactionBlocked />);

    const confirmButton = screen.getAllByRole('button', { name: '삭제' }).at(-1)!;
    expect(confirmButton).toBeDisabled();
    confirmButton.click();
    expect(onDelete).not.toHaveBeenCalled();
  });

  it('불투명 로딩 본문은 고정 색상 대신 활성 테마 배경 토큰을 사용한다', () => {
    const tokenTheme = createTheme({
      palette: { mode: 'dark', background: { paper: '#123456' } },
    });

    render(
      <ThemeProvider theme={tokenTheme}>
        <ListTable
          menuCode="CUSTOMERS"
          columns={[{ key: 'name', label: '고객사' }]}
          rows={[] as { id: number; name: string }[]}
          rowKey={(row) => row.id}
          page={0}
          pageSize={10}
          sort={{ key: 'name', direction: 'asc' }}
          onSortChange={vi.fn()}
          filters={{}}
          isLoading
        />
      </ThemeProvider>,
    );

    expect(getComputedStyle(screen.getByRole('status', { name: '목록 불러오는 중' })))
      .toHaveProperty('backgroundColor', 'rgb(18, 52, 86)');
  });

  it('모바일 최초 로딩도 오버레이가 표시될 본문 높이를 유지한다', () => {
    mocks.isMobile = true;
    renderWithTheme(
      <ListTable
        menuCode="CUSTOMERS"
        columns={[{ key: 'name', label: '고객사' }]}
        rows={[] as { id: number; name: string }[]}
        rowKey={(row) => row.id}
        page={0}
        pageSize={10}
        sort={{ key: 'name', direction: 'asc' }}
        onSortChange={vi.fn()}
        filters={{}}
        isLoading
      />,
    );

    const loader = screen.getByRole('status', { name: '목록 불러오는 중' });
    expect(loader.parentElement?.firstElementChild).toHaveStyle({ minHeight: '240px' });
  });
});
