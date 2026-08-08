import type { DemoLocation, DemoStatus } from '@/shared/demo/demoContract';
import { getCurrentPosition } from './geolocation';

type Locate = () => Promise<GeolocationPosition>;

/** 데모에서는 브라우저 GPS 권한을 요청하지 않고 status가 공개한 합성 좌표만 사용한다. */
export async function resolveAttendancePosition(
  demoStatus: DemoStatus,
  locate: Locate = getCurrentPosition,
): Promise<DemoLocation | null> {
  if (demoStatus.enabled) return demoStatus.simulatedLocation ?? null;

  const position = await locate();
  return {
    latitude: position.coords.latitude,
    longitude: position.coords.longitude,
  };
}
