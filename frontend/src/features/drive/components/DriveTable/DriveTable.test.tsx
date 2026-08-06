import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import DriveTable from './DriveTable';

describe('DriveTable', () => {
  it('폴더 행을 키보드로 연다', async () => {
    const user = userEvent.setup();
    const folder = { id: 1, name: '공용자료', createdAt: '2026-08-12T09:00:00' };
    const onOpenFolder = vi.fn();

    renderWithTheme(
      <DriveTable
        folders={[folder]}
        files={[]}
        canWrite={false}
        onOpenFolder={onOpenFolder}
        onDownloadFile={vi.fn()}
        onRenameFolder={vi.fn()}
        onDeleteFolder={vi.fn()}
        onDeleteFile={vi.fn()}
      />,
    );

    const openButton = screen.getByRole('button', { name: '공용자료' });
    openButton.focus();
    await user.keyboard('{Enter}');

    expect(onOpenFolder).toHaveBeenCalledWith(folder);
  });
});
