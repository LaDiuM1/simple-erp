import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from '@testing-library/react';
import { customerSelectColumns } from '@/features/customer/config/customerListConfig';
import type { CustomerReference } from '@/features/customer/types';
import type { ColumnConfig } from '@/shared/ui/GenericList';
import SearchTable from './SearchTable';

vi.mock('@mui/material/useMediaQuery', () => ({ default: () => false }));

vi.stubGlobal('ResizeObserver', class {
  observe() {}
  unobserve() {}
  disconnect() {}
});

let viewportWidth = 1_264;
const originalClientWidth = Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'clientWidth');

beforeAll(() => {
  Object.defineProperty(HTMLElement.prototype, 'clientWidth', {
    configurable: true,
    get: () => viewportWidth,
  });
});

beforeEach(() => {
  viewportWidth = 1_264;
});

afterAll(() => {
  if (originalClientWidth) {
    Object.defineProperty(HTMLElement.prototype, 'clientWidth', originalClientWidth);
  } else {
    Reflect.deleteProperty(HTMLElement.prototype, 'clientWidth');
  }
});

function renderCustomerTable() {
  return render(
    <SearchTable<CustomerReference>
      rows={[]}
      columns={customerSelectColumns}
      rowKey={(row) => row.id}
      rowLabel={(row) => row.name}
      isLoading={false}
      emptyMessage="검색 결과가 없습니다."
      page={0}
      pageSize={10}
      onToggleSelect={vi.fn()}
    />,
  );
}

describe('SearchTable desktop column layout', () => {
  it('고객사 모달의 고정 열을 줄이지 않고 컨테이너 폭을 정확히 채운다', () => {
    const { container } = renderCustomerTable();
    const table = container.querySelector('table');
    const columns = Array.from(container.querySelectorAll('col'));

    expect(table).toHaveStyle({ width: '1264px', minWidth: '1264px' });
    expect(columns).toHaveLength(customerSelectColumns.length + 2);
    expect(columns[0]).toHaveStyle({ width: '48px' });
    expect(columns[1]).toHaveStyle({ width: '56px' });
    expect(columns[3]).toHaveStyle({ width: '120px' });
    expect(columns[6]).toHaveStyle({ width: '112px' });
    expect(columns[7]).toHaveStyle({ width: '104px' });

    const nameWidth = Number.parseFloat((columns[2] as HTMLElement).style.width);
    const representativeWidth = Number.parseFloat((columns[4] as HTMLElement).style.width);
    const phoneWidth = Number.parseFloat((columns[5] as HTMLElement).style.width);
    expect(nameWidth / representativeWidth).toBeCloseTo(1.5);
    expect(nameWidth / phoneWidth).toBeCloseTo(1.2);
  });

  it('중간 폭에서는 의미 있는 최소 폭을 보존하고 표 자체가 가로로 확장된다', () => {
    viewportWidth = 720;
    const { container } = renderCustomerTable();
    const table = container.querySelector('table');

    expect(table).toHaveStyle({ width: '800px', minWidth: '800px' });
  });

  it('관리 모달의 No와 액션 열을 도메인 열 배분에서 제외한다', () => {
    interface Row {
      id: number;
      name: string;
      type: string;
      description: string;
    }
    const columns: ColumnConfig<Row>[] = [
      { key: 'name', label: '이름', flex: 1.2 },
      { key: 'type', label: '분류', width: 120 },
      { key: 'description', label: '설명', flex: 2 },
    ];
    const { container } = render(
      <SearchTable<Row>
        rows={[]}
        columns={columns}
        rowKey={(row) => row.id}
        isLoading={false}
        emptyMessage="등록된 항목이 없습니다."
        page={0}
        pageSize={10}
        mode="manage"
        rowActions={() => <button type="button">삭제</button>}
      />,
    );
    const tableColumns = Array.from(container.querySelectorAll('col'));

    expect(tableColumns).toHaveLength(columns.length + 2);
    expect(tableColumns[0]).toHaveStyle({ width: '56px' });
    expect(tableColumns[2]).toHaveStyle({ width: '120px' });
    expect(tableColumns.at(-1)).toHaveStyle({ width: '96px' });
  });
});
