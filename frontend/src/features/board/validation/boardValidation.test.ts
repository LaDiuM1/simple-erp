import { describe, expect, it } from 'vitest';
import { CONTENT_TEXT_MAX_LENGTH } from '@/shared/validation/contentText';
import { EMPTY_BOARD_FORM } from '@/features/board/types';
import { boardValidators } from './boardValidation';

describe('boardValidators content boundary', () => {
  it('게시글 등록·수정 본문에 공용 4,000자 상한을 적용한다', () => {
    const atLimit = '\uac00'.repeat(CONTENT_TEXT_MAX_LENGTH);
    const overLimit = '\uac00'.repeat(CONTENT_TEXT_MAX_LENGTH + 1);

    expect(boardValidators.content?.(atLimit, { ...EMPTY_BOARD_FORM, content: atLimit }))
      .toBeNull();
    expect(boardValidators.content?.(overLimit, { ...EMPTY_BOARD_FORM, content: overLimit }))
      .toBe('내용은 4,000자 이하로 입력해주세요.');
  });
});
