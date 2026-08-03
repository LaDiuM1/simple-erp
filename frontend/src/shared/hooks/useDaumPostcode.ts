import { useCallback, useState } from 'react';

declare global {
  interface Window {
    kakao?: PostcodeNamespace;
    daum?: PostcodeNamespace;
  }
}

interface PostcodeNamespace {
  Postcode: new (options: DaumPostcodeOptions) => DaumPostcodeInstance;
}

interface DaumPostcodeInstance {
  embed: (container: HTMLElement) => void;
}

const SCRIPT_ID = 'daum-postcode-script';
const SCRIPT_URL =
  'https://t1.kakaocdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
const LOAD_TIMEOUT_MS = 8_000;
const LOAD_ERROR_MESSAGE =
  '주소 검색을 불러오지 못했습니다. 다시 시도하거나 주소를 직접 입력해주세요.';

export interface DaumPostcodeData {
  zonecode: string;
  roadAddress: string;
  jibunAddress: string;
  buildingName: string;
  bname: string;
  sido: string;
  sigungu: string;
}

interface DaumPostcodeOptions {
  oncomplete: (data: DaumPostcodeData) => void;
  onclose?: () => void;
  width?: string | number;
  height?: string | number;
  maxSuggestItems?: number;
}

interface EmbedOptions {
  onComplete: (data: DaumPostcodeData) => void;
  onClose: () => void;
  signal?: AbortSignal;
}

export interface DaumPostcodeState {
  embedPostcode: (container: HTMLElement, options: EmbedOptions) => Promise<boolean>;
  isLoading: boolean;
  error: string | null;
}

let scriptPromise: Promise<void> | null = null;

function getPostcodeConstructor() {
  return window.kakao?.Postcode ?? window.daum?.Postcode;
}

function loadDaumPostcode(): Promise<void> {
  if (getPostcodeConstructor()) return Promise.resolve();
  if (scriptPromise) return scriptPromise;

  scriptPromise = new Promise<void>((resolve, reject) => {
    document.getElementById(SCRIPT_ID)?.remove();

    const script = document.createElement('script');
    script.id = SCRIPT_ID;
    script.src = SCRIPT_URL;
    script.async = true;

    const finish = (error?: Error) => {
      window.clearTimeout(timeoutId);
      script.onload = null;
      script.onerror = null;
      scriptPromise = null;
      if (error) {
        script.remove();
        reject(error);
        return;
      }
      resolve();
    };

    const timeoutId = window.setTimeout(
      () => finish(new Error(LOAD_ERROR_MESSAGE)),
      LOAD_TIMEOUT_MS,
    );

    script.onload = () => {
      if (!getPostcodeConstructor()) {
        finish(new Error(LOAD_ERROR_MESSAGE));
        return;
      }
      finish();
    };
    script.onerror = () => finish(new Error(LOAD_ERROR_MESSAGE));
    document.head.appendChild(script);
  });

  return scriptPromise;
}

/** Kakao 우편번호 위젯의 script 로딩과 WebView 호환 embed 경계를 공통 관리하는 훅. */
export function useDaumPostcode(): DaumPostcodeState {
  const [isLoading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const embedPostcode = useCallback(async (
    container: HTMLElement,
    options: EmbedOptions,
  ) => {
    setLoading(true);
    setError(null);
    try {
      await loadDaumPostcode();
      if (options.signal?.aborted) return false;
      const Postcode = getPostcodeConstructor();
      if (!Postcode) throw new Error(LOAD_ERROR_MESSAGE);
      new Postcode({
        oncomplete: options.onComplete,
        onclose: options.onClose,
        width: '100%',
        height: '100%',
        maxSuggestItems: 5,
      }).embed(container);
      return true;
    } catch (cause) {
      const message = cause instanceof Error ? cause.message : LOAD_ERROR_MESSAGE;
      setError(message);
      throw new Error(message, { cause });
    } finally {
      setLoading(false);
    }
  }, []);

  return { embedPostcode, isLoading, error };
}
