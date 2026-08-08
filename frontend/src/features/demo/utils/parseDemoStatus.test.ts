import { describe, expect, it } from 'vitest';
import { DISABLED_DEMO_STATUS } from '@/shared/demo/demoContract';
import { parseDemoStatus } from './parseDemoStatus';

const readyStatus = {
  ...DISABLED_DEMO_STATUS,
  enabled: true,
  environmentName: 'DEMO',
  stateChangedAt: '2099-08-02T06:00:00.000Z',
  generation: 'generation-a',
  lastResetAt: '2099-08-02T06:00:00.000Z',
  nextResetAt: '2099-08-02T12:00:00.000Z',
  notice: '모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.',
  uploadEnabled: true,
  simulatedLocation: { latitude: 37.5663, longitude: 126.9779 },
  publicAccounts: [{
    label: '관리자',
    description: '전체 데모 흐름',
    loginId: 'demo.manager',
    password: 'public-password',
    recommended: true,
  }],
};

describe('parseDemoStatus', () => {
  it('완전한 READY 계약만 통과시킨다', () => {
    expect(parseDemoStatus(readyStatus)).toEqual(readyStatus);
  });

  it('업로드 capability는 READY 상태 계약에서 명시적으로 전달한다', () => {
    expect(parseDemoStatus({ ...readyStatus, uploadEnabled: false }).uploadEnabled).toBe(false);
    expect(parseDemoStatus(readyStatus).uploadEnabled).toBe(true);
  });

  it('모의 위치를 사용하지 않는 READY 상태도 업무 화면에 진입시킨다', () => {
    const status = parseDemoStatus({ ...readyStatus, simulatedLocation: undefined });

    expect(status.simulatedLocation).toBeUndefined();
  });

  it.each([
    ['unknown state', { ...readyStatus, state: 'UNKNOWN' }],
    ['missing accounts', { ...readyStatus, publicAccounts: [] }],
    ['invalid reset boundary', {
      ...readyStatus,
      warningBeforeSeconds: 60,
      writeLockBeforeSeconds: 120,
    }],
    ['invalid date', { ...readyStatus, nextResetAt: 'not-a-date' }],
    ['missing state timestamp', { ...readyStatus, stateChangedAt: null }],
    ['invalid state transition order', {
      ...readyStatus,
      stateChangedAt: '2099-08-02T05:59:59.000Z',
    }],
    ['invalid reset schedule order', {
      ...readyStatus,
      nextResetAt: '2099-08-02T06:00:00.000Z',
    }],
    ['invalid location', {
      ...readyStatus,
      simulatedLocation: { latitude: 100, longitude: 126.9779 },
    }],
  ])('잘못된 200 응답을 거부한다: %s', (_label, payload) => {
    expect(() => parseDemoStatus(payload)).toThrow(/Invalid demo status/);
  });

  it('maintenance 상태에는 검증 중인 candidate generation이 필요하다', () => {
    expect(() => parseDemoStatus({
      ...readyStatus,
      state: 'VERIFYING',
      generation: null,
      candidateGeneration: null,
      lastResetAt: null,
      writeLocked: true,
    })).toThrow(/lifecycle contract/);
  });

  it('서버가 직접 합성한 FAILED 상태는 candidate generation 없이 잠근다', () => {
    const status = parseDemoStatus({
      ...readyStatus,
      state: 'FAILED',
      generation: null,
      candidateGeneration: null,
      lastResetAt: null,
      nextResetAt: null,
      publicAccounts: [],
      writeLocked: true,
    });

    expect(status.state).toBe('FAILED');
    expect(status.writeLocked).toBe(true);
  });

  it('초기화 도중 전환된 FAILED 상태는 candidate generation을 보존한다', () => {
    const status = parseDemoStatus({
      ...readyStatus,
      state: 'FAILED',
      generation: null,
      candidateGeneration: 'candidate-generation-b',
      lastResetAt: null,
      nextResetAt: null,
      publicAccounts: [],
      writeLocked: true,
    });

    expect(status.state).toBe('FAILED');
    expect(status.candidateGeneration).toBe('candidate-generation-b');
  });

  it.each([
    ['write lock', { ...DISABLED_DEMO_STATUS, writeLocked: true }],
    ['demo accounts', { ...DISABLED_DEMO_STATUS, publicAccounts: readyStatus.publicAccounts }],
    ['simulated location', {
      ...DISABLED_DEMO_STATUS,
      simulatedLocation: readyStatus.simulatedLocation,
    }],
    ['upload disabled', { ...DISABLED_DEMO_STATUS, uploadEnabled: false }],
  ])('비활성 응답에 데모 전용 상태가 섞이면 거부한다: %s', (_label, payload) => {
    expect(() => parseDemoStatus(payload)).toThrow(/disabled contract/);
  });
});
