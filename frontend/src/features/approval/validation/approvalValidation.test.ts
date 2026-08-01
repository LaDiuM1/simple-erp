import { describe, expect, it } from 'vitest';
import { CONTENT_TEXT_MAX_LENGTH } from '@/shared/validation/contentText';
import { EMPTY_APPROVAL_FORM } from '@/features/approval/types';
import { approvalValidators } from './approvalValidation';

describe('approvalValidators content boundary', () => {
  it('일반 기안 상신 본문에 공용 4,000자 상한을 적용한다', () => {
    const atLimit = '\uac00'.repeat(CONTENT_TEXT_MAX_LENGTH);
    const overLimit = '\uac00'.repeat(CONTENT_TEXT_MAX_LENGTH + 1);

    expect(approvalValidators.content?.(atLimit, { ...EMPTY_APPROVAL_FORM, content: atLimit }))
      .toBeNull();
    expect(approvalValidators.content?.(overLimit, { ...EMPTY_APPROVAL_FORM, content: overLimit }))
      .toBe('본문은 4,000자 이하로 입력해주세요.');
  });
});
