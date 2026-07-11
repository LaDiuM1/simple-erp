/**
 * 브라우저 geolocation 을 promise 로 래핑 — 미지원 / 권한 거부 / 실패는 reject.
 * 출퇴근 체크는 좌표가 필수라 실패 시 호출부가 요청 자체를 중단한다.
 */
export function getCurrentPosition(): Promise<GeolocationPosition> {
  return new Promise((resolve, reject) => {
    if (!('geolocation' in navigator)) {
      reject(new Error('이 브라우저는 위치 정보를 지원하지 않습니다.'));
      return;
    }
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true,
      timeout: 10_000,
      maximumAge: 0,
    });
  });
}

/**
 * getCurrentPosition reject 값 → 사용자 안내 메시지.
 * GeolocationPositionError.code (1 권한 거부 / 2 위치 확인 불가 / 3 시간 초과) 로 분기,
 * 미지원 브라우저 (Error) 는 메시지 그대로 노출.
 */
export function geolocationErrorMessage(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'code' in error) {
    switch ((error as { code: unknown }).code) {
      case 1:
        return '위치 권한이 거부되었습니다. 브라우저 설정에서 위치 권한을 허용해주세요.';
      case 2:
        return '현재 위치를 확인할 수 없습니다. 잠시 후 다시 시도해주세요.';
      case 3:
        return '위치 확인 시간이 초과되었습니다. 다시 시도해주세요.';
    }
  }
  if (error instanceof Error && error.message !== '') return error.message;
  return '위치 정보를 가져올 수 없습니다. 브라우저 위치 권한을 확인해주세요.';
}
