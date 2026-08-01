import { fireEvent, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CONTENT_TEXT_MAX_LENGTH } from '@/shared/validation/contentText';
import { renderWithTheme } from '@/test/renderWithTheme';
import ContentTextField from './ContentTextField';

describe('ContentTextField', () => {
  it('textarea 에 4,000자 maxlength 와 현재 글자 수를 노출한다', () => {
    renderWithTheme(
      <ContentTextField label="본문" value="테스트" onChange={vi.fn()} />,
    );

    expect(screen.getByLabelText('본문')).toHaveAttribute(
      'maxlength',
      String(CONTENT_TEXT_MAX_LENGTH),
    );
    expect(screen.getByText('3 / 4,000자')).toBeInTheDocument();
  });

  it('상한 초과 값이 들어오면 blur 전에 즉시 오류를 노출한다', () => {
    const onChange = vi.fn();
    const { rerender } = renderWithTheme(
      <ContentTextField label="본문" value="" onChange={onChange} />,
    );

    const overLimit = '가'.repeat(CONTENT_TEXT_MAX_LENGTH + 1);
    fireEvent.change(screen.getByLabelText('본문'), { target: { value: overLimit } });
    expect(onChange).toHaveBeenCalledWith(overLimit);

    rerender(
      <ContentTextField label="본문" value={overLimit} onChange={onChange} />,
    );
    expect(screen.getByText('본문은 4,000자 이하로 입력해주세요.')).toBeInTheDocument();
    expect(screen.getByLabelText('본문')).toHaveAttribute('aria-invalid', 'true');
  });
});
