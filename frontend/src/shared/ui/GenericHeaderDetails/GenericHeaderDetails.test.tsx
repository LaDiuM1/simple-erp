import { describe, expect, it } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import GenericHeaderDetails from './GenericHeaderDetails';

const fields = [{ label: '계약 번호', value: 'CT2026-001' }];

describe('GenericHeaderDetails sticky policy', () => {
  it('기본 상세 헤더는 기존 sticky 동작을 유지한다', () => {
    const { container } = renderWithTheme(<GenericHeaderDetails fields={fields} />);

    expect(container.firstElementChild).toHaveStyle({ position: 'sticky' });
  });

  it('sticky 해제 시 후속 영역과 함께 문서 흐름으로 스크롤된다', () => {
    const { container } = renderWithTheme(
      <GenericHeaderDetails fields={fields} sticky={false} />,
    );

    expect(container.firstElementChild).toHaveStyle({
      position: 'static',
      boxShadow: 'none',
    });
  });
});
