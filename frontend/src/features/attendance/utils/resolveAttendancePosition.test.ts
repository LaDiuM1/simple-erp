import { describe, expect, it, vi } from 'vitest';
import { DISABLED_DEMO_STATUS } from '@/shared/demo/demoContract';
import { resolveAttendancePosition } from './resolveAttendancePosition';

describe('resolveAttendancePosition', () => {
  it('데모에서는 브라우저 위치 API를 호출하지 않고 합성 좌표를 사용한다', async () => {
    const locate = vi.fn();
    const result = await resolveAttendancePosition({
      ...DISABLED_DEMO_STATUS,
      enabled: true,
      simulatedLocation: { latitude: 37.5663, longitude: 126.9779 },
    }, locate);

    expect(result).toEqual({ latitude: 37.5663, longitude: 126.9779 });
    expect(locate).not.toHaveBeenCalled();
  });

  it('데모 합성 좌표가 없으면 실제 GPS로 우회하지 않는다', async () => {
    const locate = vi.fn();
    const result = await resolveAttendancePosition({
      ...DISABLED_DEMO_STATUS,
      enabled: true,
    }, locate);

    expect(result).toBeNull();
    expect(locate).not.toHaveBeenCalled();
  });

  it('일반 환경에서만 브라우저 위치를 좌표 값으로 변환한다', async () => {
    const locate = vi.fn().mockResolvedValue({
      coords: { latitude: 35.1, longitude: 129.1 },
    } as GeolocationPosition);

    await expect(resolveAttendancePosition(DISABLED_DEMO_STATUS, locate))
      .resolves.toEqual({ latitude: 35.1, longitude: 129.1 });
    expect(locate).toHaveBeenCalledOnce();
  });
});
