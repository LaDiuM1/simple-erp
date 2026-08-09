import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AcquisitionSourceInfo } from '@/features/acquisitionSource/types';
import AcquisitionSourceSearchModal from './AcquisitionSourceSearchModal';

const mocks = vi.hoisted(() => ({ useQuery: vi.fn() }));

vi.mock('@/features/acquisitionSource/api/acquisitionSourceApi', () => ({
  useGetAcquisitionSourcesQuery: () => mocks.useQuery(),
}));
vi.mock('@mui/material/useMediaQuery', () => ({ default: () => false }));

vi.stubGlobal('ResizeObserver', class {
  observe() {}
  disconnect() {}
});

const sources: AcquisitionSourceInfo[] = [
  { id: 1, name: '전시회 문의', type: 'EXHIBITION', description: null },
  { id: 2, name: '기존 고객 소개', type: 'REFERRAL', description: null },
];

const refetch = vi.fn();
let queryState: Record<string, unknown>;

function loadingState() {
  return {
    data: undefined,
    currentData: undefined,
    isLoading: true,
    isFetching: true,
    isError: false,
    error: undefined,
    refetch,
  };
}

function readyState(isFetching = false) {
  return {
    data: sources,
    currentData: sources,
    isLoading: false,
    isFetching,
    isError: false,
    error: undefined,
    refetch,
  };
}

describe('AcquisitionSourceSearchModal', () => {
  beforeEach(() => {
    refetch.mockReset();
    queryState = loadingState();
    mocks.useQuery.mockReset().mockImplementation(() => queryState);
  });

  it('후보가 늦게 도착해도 초기 선택을 복원하고 이후 갱신에는 사용자 선택을 보존한다', async () => {
    const user = userEvent.setup();
    const initialIds = [2];
    const props = {
      open: true,
      onClose: vi.fn(),
      context: 'form' as const,
      initialIds,
      onConfirm: vi.fn(),
    };
    const { rerender } = render(<AcquisitionSourceSearchModal {...props} />);

    expect(screen.queryByRole('checkbox', { name: '기존 고객 소개' })).not.toBeInTheDocument();

    queryState = readyState();
    rerender(<AcquisitionSourceSearchModal {...props} />);
    expect(screen.getByRole('checkbox', { name: '기존 고객 소개' })).toBeChecked();

    await user.click(screen.getByRole('checkbox', { name: '전시회 문의' }));
    expect(screen.getByRole('checkbox', { name: '전시회 문의' })).toBeChecked();

    queryState = readyState(true);
    rerender(<AcquisitionSourceSearchModal {...props} />);
    expect(screen.getByRole('checkbox', { name: '기존 고객 소개' })).toBeChecked();
    expect(screen.getByRole('checkbox', { name: '전시회 문의' })).toBeChecked();
  });
});
