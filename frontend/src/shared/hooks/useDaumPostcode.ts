declare global {
  interface Window {
    daum?: {
      Postcode: new (options: DaumPostcodeOptions) => { open: () => void };
    };
  }
}

const SCRIPT_URL =
  'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';

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
}

let scriptPromise: Promise<void> | null = null;

function loadDaumPostcode(): Promise<void> {
  if (window.daum?.Postcode) return Promise.resolve();
  if (scriptPromise) return scriptPromise;

  scriptPromise = new Promise<void>((resolve, reject) => {
    const script = document.createElement('script');
    script.src = SCRIPT_URL;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => {
      scriptPromise = null;
      reject(new Error('Daum 우편번호 스크립트 로드 실패'));
    };
    document.head.appendChild(script);
  });
  return scriptPromise;
}

/**
 * Daum 우편번호 검색 위젯을 띄우는 훅.
 * 첫 호출 시점에 스크립트를 lazy 로드하고, 이후 호출은 즉시 모달을 연다.
 * API 키 / 사용량 제한 없이 무료 사용.
 */
export function useDaumPostcode() {
  return async (onComplete: (data: DaumPostcodeData) => void) => {
    await loadDaumPostcode();
    if (!window.daum?.Postcode) {
      throw new Error('Daum 우편번호를 사용할 수 없습니다.');
    }
    new window.daum.Postcode({ oncomplete: onComplete }).open();
  };
}
