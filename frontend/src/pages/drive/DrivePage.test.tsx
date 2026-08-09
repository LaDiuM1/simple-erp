import { screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { renderWithTheme } from '@/test/renderWithTheme';
import DrivePage from './DrivePage';

const mocks = vi.hoisted(() => ({
  useDrivePage: vi.fn(),
}));

vi.mock('@/features/drive/hooks/useDrivePage', () => ({
  useDrivePage: mocks.useDrivePage,
}));
vi.mock('@/shared/ui/layout/PageHeaderActions', () => ({ default: () => null }));
vi.mock('@/features/drive/components/DriveTable/DriveTable', () => ({
  default: () => <div>드라이브 목록</div>,
}));
vi.mock('@/features/drive/components/DriveModals/DriveModals', () => ({ default: () => null }));
vi.mock('@/features/drive/components/DriveBreadcrumb/DriveBreadcrumb', () => ({
  default: ({ breadcrumb }: { breadcrumb: Array<{ name: string }> }) => (
    <div data-testid="drive-breadcrumb">{breadcrumb.map((item) => item.name).join(' / ')}</div>
  ),
}));

function driveState(breadcrumb: Array<{ id: number; name: string }>) {
  const noop = vi.fn();
  return {
    queries: {
      browse: {
        data: { breadcrumb, folders: [], files: [] },
        currentData: { breadcrumb, folders: [], files: [] },
        isLoading: false,
        isError: false,
      },
    },
    canWrite: false,
    uploadEnabled: false,
    headerActions: [],
    onNavigateFolder: noop,
    onOpenFolder: noop,
    onDownloadFile: noop,
    onRenameFolder: noop,
    onDeleteFolder: noop,
    onDeleteFile: noop,
    uploadInputRef: { current: null },
    onUploadFileSelected: noop,
    modal: {},
  };
}

describe('DrivePage', () => {
  beforeEach(() => {
    mocks.useDrivePage.mockReturnValue(driveState([]));
  });

  it('루트에서는 중복 경로 행을 렌더하지 않는다', () => {
    renderWithTheme(
      <MemoryRouter>
        <DrivePage />
      </MemoryRouter>,
    );

    expect(screen.queryByTestId('drive-breadcrumb')).not.toBeInTheDocument();
    expect(screen.getByText('드라이브 목록')).toBeInTheDocument();
  });

  it('하위 폴더에서는 현재 경로를 유지한다', () => {
    mocks.useDrivePage.mockReturnValue(driveState([{ id: 1, name: '공용자료' }]));

    renderWithTheme(
      <MemoryRouter>
        <DrivePage />
      </MemoryRouter>,
    );

    expect(screen.getByTestId('drive-breadcrumb')).toHaveTextContent('공용자료');
  });
});
