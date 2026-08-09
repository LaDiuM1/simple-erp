import { describe, expect, it } from 'vitest';
import { getErrorMessage } from './error';

describe('getErrorMessage', () => {
  it.each([
    ['DEMO_RESET_IN_PROGRESS', '데모 데이터를 초기화하거나 검증하고 있습니다. 잠시 후 다시 시도해 주세요.'],
    ['DEMO_UPLOAD_DISABLED', '데모에서는 파일 업로드가 비활성화되어 있습니다.'],
    ['DEMO_PROTECTED_RESOURCE', '데모 복구에 필요한 보호 항목은 변경할 수 없습니다.'],
    ['DEMO_RATE_LIMIT_EXCEEDED', '데모 요청 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.'],
  ])('%s 코드를 안정적인 데모 안내로 변환', (code, expected) => {
    expect(getErrorMessage({ status: 503, message: 'server message', code })).toBe(expected);
  });

  it('알 수 없는 코드는 서버 메시지를 보존', () => {
    expect(getErrorMessage({ status: 400, message: '입력값을 확인해 주세요.', code: 'UNKNOWN' }))
      .toBe('입력값을 확인해 주세요.');
  });
});
