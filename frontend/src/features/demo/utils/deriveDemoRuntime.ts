import type { DemoStatus } from '@/shared/demo/demoContract';
import { millisecondsUntil } from './countdown';

export interface DemoRuntime {
  remainingMs: number | null;
  resetSoon: boolean;
  writeLocked: boolean;
  writeBlocked: boolean;
  uploadEnabled: boolean;
  maintenance: boolean;
  failed: boolean;
}

export function deriveDemoRuntime(
  status: DemoStatus,
  statusResolved: boolean,
  statusUnavailable: boolean,
  nowMs: number,
): DemoRuntime {
  const remainingMs = millisecondsUntil(status.nextResetAt, nowMs);
  const resetSoon = status.enabled
    && remainingMs !== null
    && remainingMs <= status.warningBeforeSeconds * 1_000;
  const locallyWriteLocked = status.enabled
    && remainingMs !== null
    && remainingMs <= status.writeLockBeforeSeconds * 1_000;
  const writeLocked = status.enabled && (status.writeLocked || locallyWriteLocked);
  const maintenance = status.enabled
    && (status.state === 'RESETTING' || status.state === 'VERIFYING');
  const expired = status.enabled
    && status.state === 'READY'
    && remainingMs !== null
    && remainingMs <= 0;
  const failed = status.enabled && (status.state === 'FAILED' || expired);
  const writeBlocked = !statusResolved
    || statusUnavailable
    || (status.enabled && (writeLocked || maintenance || failed));
  const uploadEnabled = !status.enabled || (status.uploadEnabled && !writeBlocked);

  return {
    remainingMs,
    resetSoon,
    writeLocked,
    writeBlocked,
    uploadEnabled,
    maintenance,
    failed,
  };
}
