import { describe, expect, it, vi } from 'vitest';
import { screen } from '@testing-library/react';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { renderWithTheme } from '@/test/renderWithTheme';
import MenuPermissionMatrix from './MenuPermissionMatrix';

describe('MenuPermissionMatrix', () => {
  it('지원하지 않는 메뉴의 데이터 범위를 전체로 고정한다', () => {
    renderWithTheme(
      <MenuPermissionMatrix
        permissions={{
          [MENU_CODE.CUSTOMERS]: { canRead: true, canWrite: false, dataScope: 'SELF' },
          [MENU_CODE.EMPLOYEES]: { canRead: true, canWrite: false, dataScope: 'SELF' },
        }}
        onChange={vi.fn()}
      />,
    );

    expect(screen.getByLabelText('고객사 관리 데이터 범위')).toBeEnabled();
    expect(screen.getByLabelText('직원 관리 데이터 범위')).toHaveAttribute(
      'aria-disabled',
      'true',
    );
    expect(screen.getByLabelText('직원 관리 데이터 범위')).toHaveTextContent('전체');
  });
});
