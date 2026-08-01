import { describe, expect, it } from 'vitest';
import { CONTENT_TEXT_MAX_LENGTH, validateContentText } from './contentText';

describe('validateContentText', () => {
  it('4,000자 한글 본문을 허용한다', () => {
    expect(validateContentText('가'.repeat(CONTENT_TEXT_MAX_LENGTH), '본문')).toBeNull();
  });

  it('4,001자 한글 본문을 차단한다', () => {
    expect(validateContentText('가'.repeat(CONTENT_TEXT_MAX_LENGTH + 1), '본문'))
      .toBe('본문은 4,000자 이하로 입력해주세요.');
  });

  it('필수 본문의 공백 입력을 차단한다', () => {
    expect(validateContentText('   ', '내용', true)).toBe('내용을 입력해주세요.');
  });
});
