import { createContext, useContext } from 'react';
import { DISABLED_DEMO_STATUS, type DemoStatus } from './demoContract';

export interface DemoContextValue {
  status: DemoStatus;
  statusResolved: boolean;
  statusUnavailable: boolean;
  resetSoon: boolean;
  remainingMs: number | null;
  countdown: string;
  writeLocked: boolean;
  writeBlocked: boolean;
  uploadEnabled: boolean;
  maintenance: boolean;
  failed: boolean;
}

export const DEFAULT_DEMO_CONTEXT: DemoContextValue = {
  status: DISABLED_DEMO_STATUS,
  statusResolved: false,
  statusUnavailable: false,
  resetSoon: false,
  remainingMs: null,
  countdown: '--:--:--',
  writeLocked: false,
  writeBlocked: true,
  uploadEnabled: false,
  maintenance: false,
  failed: false,
};

export const DemoContext = createContext<DemoContextValue>(DEFAULT_DEMO_CONTEXT);

export function useDemo(): DemoContextValue {
  return useContext(DemoContext);
}
