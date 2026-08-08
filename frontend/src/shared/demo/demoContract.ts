export type DemoServerState = 'READY' | 'RESETTING' | 'VERIFYING' | 'FAILED';

export interface DemoPublicAccount {
  label: string;
  description: string;
  loginId: string;
  password: string;
  recommended: boolean;
}

export interface DemoLocation {
  latitude: number;
  longitude: number;
}

export interface DemoStatus {
  enabled: boolean;
  environmentName: string;
  state: DemoServerState;
  stateChangedAt: string | null;
  generation: string | null;
  candidateGeneration?: string | null;
  lastResetAt: string | null;
  nextResetAt: string | null;
  warningBeforeSeconds: number;
  writeLockBeforeSeconds: number;
  writeLocked: boolean;
  notice: string;
  uploadEnabled: boolean;
  simulatedLocation?: DemoLocation;
  publicAccounts: DemoPublicAccount[];
}

export const DISABLED_DEMO_STATUS: DemoStatus = {
  enabled: false,
  environmentName: 'PRODUCTION',
  state: 'READY',
  stateChangedAt: null,
  generation: null,
  candidateGeneration: null,
  lastResetAt: null,
  nextResetAt: null,
  warningBeforeSeconds: 300,
  writeLockBeforeSeconds: 120,
  writeLocked: false,
  notice: '',
  uploadEnabled: true,
  publicAccounts: [],
};
