import { describe, expect, it } from 'vitest';
import { DISABLED_DEMO_STATUS, type DemoStatus } from '@/shared/demo/demoContract';
import { deriveDemoRuntime } from './deriveDemoRuntime';

const NOW = Date.parse('2026-08-02T10:00:00.000Z');

function readyStatus(remainingSeconds: number): DemoStatus {
  return {
    ...DISABLED_DEMO_STATUS,
    enabled: true,
    environmentName: 'DEMO',
    stateChangedAt: new Date(NOW - 1_000).toISOString(),
    generation: 'generation-a',
    lastResetAt: new Date(NOW - 1_000).toISOString(),
    nextResetAt: new Date(NOW + remainingSeconds * 1_000).toISOString(),
    notice: '모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.',
    uploadEnabled: true,
    simulatedLocation: { latitude: 37.5663, longitude: 126.9779 },
    publicAccounts: [{
      label: '관리자',
      description: '전체 데모 체험',
      loginId: 'demo.manager',
      password: 'public-password',
      recommended: true,
    }],
  };
}

describe('deriveDemoRuntime', () => {
  it('2분 경계 직전에는 조회 상태를 유지하면서 쓰기만 잠근다', () => {
    const before = deriveDemoRuntime(readyStatus(121), true, false, NOW);
    const boundary = deriveDemoRuntime(readyStatus(120), true, false, NOW);

    expect(before).toMatchObject({ writeLocked: false, writeBlocked: false, failed: false });
    expect(before.uploadEnabled).toBe(true);
    expect(boundary).toMatchObject({
      writeLocked: true,
      writeBlocked: true,
      uploadEnabled: false,
      maintenance: false,
      failed: false,
    });
  });

  it('READY 만료와 status 미확인은 fail-closed로 처리한다', () => {
    expect(deriveDemoRuntime(readyStatus(0), true, false, NOW))
      .toMatchObject({ writeBlocked: true, failed: true });
    expect(deriveDemoRuntime(DISABLED_DEMO_STATUS, false, false, NOW))
      .toMatchObject({ writeBlocked: true });
  });

  it('정상 비활성 계약은 일반 운영의 쓰기와 업로드를 열어 둔다', () => {
    expect(deriveDemoRuntime(DISABLED_DEMO_STATUS, true, false, NOW))
      .toMatchObject({ writeBlocked: false, uploadEnabled: true, failed: false });
  });
});
