import {
  DEFAULT_DEMO_CONTEXT,
  type DemoContextValue,
} from '@/shared/demo/DemoContext';
import { DISABLED_DEMO_STATUS } from '@/shared/demo/demoContract';

export const READY_DEMO_CONTEXT: DemoContextValue = {
  ...DEFAULT_DEMO_CONTEXT,
  statusResolved: true,
  status: {
    ...DISABLED_DEMO_STATUS,
    enabled: true,
    environmentName: 'DEMO',
    stateChangedAt: '2099-08-02T06:00:00.000Z',
    generation: 'generation-a',
    lastResetAt: '2099-08-02T06:00:00.000Z',
    nextResetAt: '2099-08-02T12:00:00.000Z',
    notice: '합성 데이터 데모',
    uploadEnabled: true,
    simulatedLocation: { latitude: 37.5663, longitude: 126.9779 },
    publicAccounts: [{
      label: '관리자',
      description: '전체 데모 흐름',
      loginId: 'demo.manager',
      password: 'public-password',
      recommended: true,
    }],
  },
  writeLocked: false,
  writeBlocked: false,
  uploadEnabled: true,
};

/** 서버 capability는 유지하고 reset의 effective write lock만 적용한 유효한 READY 경계. */
export const WRITE_LOCKED_DEMO_CONTEXT: DemoContextValue = {
  ...READY_DEMO_CONTEXT,
  status: {
    ...READY_DEMO_CONTEXT.status,
    writeLocked: true,
  },
  writeLocked: true,
  writeBlocked: true,
  uploadEnabled: false,
};
