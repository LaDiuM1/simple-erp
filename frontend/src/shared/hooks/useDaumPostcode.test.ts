import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useDaumPostcode, type DaumPostcodeData } from './useDaumPostcode';

const SCRIPT_SELECTOR = '#daum-postcode-script';

describe('useDaumPostcode', () => {
  beforeEach(() => {
    document.querySelector(SCRIPT_SELECTOR)?.remove();
    delete window.kakao;
    delete window.daum;
  });

  afterEach(() => {
    document.querySelector(SCRIPT_SELECTOR)?.remove();
    delete window.kakao;
    delete window.daum;
    vi.useRealTimers();
  });

  it('script 실패 후 새 script 로 재시도해 현재 문서의 container 에 embed 한다', async () => {
    const embed = vi.fn();
    const postcodeOptions = vi.fn();
    class PostcodeMock {
      constructor(options: {
        oncomplete: (data: DaumPostcodeData) => void;
        onclose?: () => void;
      }) {
        postcodeOptions(options);
      }

      embed(container: HTMLElement) {
        embed(container);
      }
    }
    const container = document.createElement('div');
    const onComplete = vi.fn();
    const onClose = vi.fn();
    const { result } = renderHook(() => useDaumPostcode());

    let firstAttempt!: Promise<boolean>;
    act(() => {
      firstAttempt = result.current.embedPostcode(container, { onComplete, onClose });
    });
    const firstScript = document.querySelector<HTMLScriptElement>(SCRIPT_SELECTOR);
    expect(firstScript).not.toBeNull();
    await act(async () => {
      firstScript?.dispatchEvent(new Event('error'));
      await expect(firstAttempt).rejects.toThrow('주소 검색을 불러오지 못했습니다.');
    });
    expect(result.current.error).toContain('주소를 직접 입력');
    expect(document.querySelector(SCRIPT_SELECTOR)).toBeNull();

    let retryAttempt!: Promise<boolean>;
    act(() => {
      retryAttempt = result.current.embedPostcode(container, { onComplete, onClose });
    });
    const retryScript = document.querySelector<HTMLScriptElement>(SCRIPT_SELECTOR);
    expect(retryScript).not.toBeNull();
    expect(retryScript).not.toBe(firstScript);
    window.kakao = { Postcode: PostcodeMock };
    await act(async () => {
      retryScript?.dispatchEvent(new Event('load'));
      await expect(retryAttempt).resolves.toBe(true);
    });

    expect(result.current.error).toBeNull();
    expect(postcodeOptions).toHaveBeenCalledWith(expect.objectContaining({
      oncomplete: onComplete,
      onclose: onClose,
      width: '100%',
      height: '100%',
    }));
    expect(embed).toHaveBeenCalledWith(container);
  });

  it('script 응답이 없으면 시간 제한 후 실패 상태로 전환한다', async () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useDaumPostcode());

    let attempt!: Promise<boolean>;
    act(() => {
      attempt = result.current.embedPostcode(document.createElement('div'), {
        onComplete: vi.fn(),
        onClose: vi.fn(),
      });
    });
    await act(async () => {
      vi.runAllTimers();
      await expect(attempt).rejects.toThrow('주소 검색을 불러오지 못했습니다.');
    });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toContain('주소를 직접 입력');
    expect(document.querySelector(SCRIPT_SELECTOR)).toBeNull();
  });

  it('기존 daum namespace 만 제공되는 환경도 embed 호환성을 유지한다', async () => {
    const embed = vi.fn();
    class LegacyPostcodeMock {
      embed(container: HTMLElement) {
        embed(container);
      }
    }
    window.daum = { Postcode: LegacyPostcodeMock };
    const container = document.createElement('div');
    const { result } = renderHook(() => useDaumPostcode());

    await act(async () => {
      await result.current.embedPostcode(container, {
        onComplete: vi.fn(),
        onClose: vi.fn(),
      });
    });

    expect(embed).toHaveBeenCalledWith(container);
    expect(document.querySelector(SCRIPT_SELECTOR)).toBeNull();
  });

  it('Dialog 가 닫힌 뒤 완료된 script 로 stale container 를 embed 하지 않는다', async () => {
    const embed = vi.fn();
    class PostcodeMock {
      embed(container: HTMLElement) {
        embed(container);
      }
    }
    const controller = new AbortController();
    const { result } = renderHook(() => useDaumPostcode());
    let attempt!: Promise<boolean>;
    act(() => {
      attempt = result.current.embedPostcode(document.createElement('div'), {
        onComplete: vi.fn(),
        onClose: vi.fn(),
        signal: controller.signal,
      });
    });
    controller.abort();
    window.kakao = { Postcode: PostcodeMock };
    const script = document.querySelector<HTMLScriptElement>(SCRIPT_SELECTOR);

    await act(async () => {
      script?.dispatchEvent(new Event('load'));
      await expect(attempt).resolves.toBe(false);
    });

    expect(embed).not.toHaveBeenCalled();
  });
});
