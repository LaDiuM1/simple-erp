import type {
  DemoLocation,
  DemoPublicAccount,
  DemoServerState,
  DemoStatus,
} from '@/shared/demo/demoContract';

const SERVER_STATES = new Set<DemoServerState>([
  'READY',
  'RESETTING',
  'VERIFYING',
  'FAILED',
]);

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function requireBoolean(value: unknown, field: string): boolean {
  if (typeof value !== 'boolean') throw new Error(`Invalid demo status: ${field}`);
  return value;
}

function requireString(value: unknown, field: string, allowEmpty = false): string {
  if (typeof value !== 'string' || (!allowEmpty && value.trim() === '')) {
    throw new Error(`Invalid demo status: ${field}`);
  }
  return value;
}

function nullableString(value: unknown, field: string): string | null {
  if (value === null) return null;
  return requireString(value, field);
}

function optionalNullableString(value: unknown, field: string): string | null | undefined {
  if (value === undefined || value === null) return value;
  return requireString(value, field);
}

function requirePositiveInteger(value: unknown, field: string): number {
  if (!Number.isInteger(value) || (value as number) <= 0) {
    throw new Error(`Invalid demo status: ${field}`);
  }
  return value as number;
}

function requireDateTime(value: unknown, field: string): string | null {
  const dateTime = nullableString(value, field);
  if (dateTime !== null && !Number.isFinite(Date.parse(dateTime))) {
    throw new Error(`Invalid demo status: ${field}`);
  }
  return dateTime;
}

function parseLocation(value: unknown): DemoLocation | undefined {
  if (value === undefined || value === null) return undefined;
  if (!isRecord(value)) throw new Error('Invalid demo status: simulatedLocation');
  const { latitude, longitude } = value;
  if (typeof latitude !== 'number' || !Number.isFinite(latitude)
      || latitude < -90 || latitude > 90
      || typeof longitude !== 'number' || !Number.isFinite(longitude)
      || longitude < -180 || longitude > 180) {
    throw new Error('Invalid demo status: simulatedLocation');
  }
  return { latitude, longitude };
}

function parsePublicAccounts(value: unknown): DemoPublicAccount[] {
  if (!Array.isArray(value)) throw new Error('Invalid demo status: publicAccounts');
  const seenLoginIds = new Set<string>();
  return value.map((account, index) => {
    if (!isRecord(account)) throw new Error(`Invalid demo status: publicAccounts[${index}]`);
    const loginId = requireString(account.loginId, `publicAccounts[${index}].loginId`);
    if (seenLoginIds.has(loginId)) throw new Error('Invalid demo status: duplicate demo account');
    seenLoginIds.add(loginId);
    return {
      label: requireString(account.label, `publicAccounts[${index}].label`),
      description: requireString(
        account.description,
        `publicAccounts[${index}].description`,
        true,
      ),
      loginId,
      password: requireString(account.password, `publicAccounts[${index}].password`),
      recommended: requireBoolean(
        account.recommended,
        `publicAccounts[${index}].recommended`,
      ),
    };
  });
}

/** 정적 control-plane 응답을 신뢰 경계에서 검증해 잘못된 200 응답도 fail-closed시킨다. */
export function parseDemoStatus(value: unknown): DemoStatus {
  if (!isRecord(value)) throw new Error('Invalid demo status payload');

  const enabled = requireBoolean(value.enabled, 'enabled');
  const state = value.state;
  if (typeof state !== 'string' || !SERVER_STATES.has(state as DemoServerState)) {
    throw new Error('Invalid demo status: state');
  }

  const warningBeforeSeconds = requirePositiveInteger(
    value.warningBeforeSeconds,
    'warningBeforeSeconds',
  );
  const writeLockBeforeSeconds = requirePositiveInteger(
    value.writeLockBeforeSeconds,
    'writeLockBeforeSeconds',
  );
  if (warningBeforeSeconds < writeLockBeforeSeconds) {
    throw new Error('Invalid demo status: reset boundaries');
  }

  const generation = nullableString(value.generation, 'generation');
  const candidateGeneration = optionalNullableString(
    value.candidateGeneration,
    'candidateGeneration',
  );
  const lastResetAt = requireDateTime(value.lastResetAt, 'lastResetAt');
  const nextResetAt = requireDateTime(value.nextResetAt, 'nextResetAt');
  const stateChangedAt = requireDateTime(value.stateChangedAt, 'stateChangedAt');
  const publicAccounts = parsePublicAccounts(value.publicAccounts);
  const uploadEnabled = requireBoolean(value.uploadEnabled, 'uploadEnabled');
  const simulatedLocation = parseLocation(value.simulatedLocation);
  const environmentName = requireString(value.environmentName, 'environmentName');
  const writeLocked = requireBoolean(value.writeLocked, 'writeLocked');
  const notice = requireString(value.notice, 'notice', !enabled);

  if (enabled && state === 'READY') {
    if (!stateChangedAt || !generation || candidateGeneration != null
        || !lastResetAt || !nextResetAt || publicAccounts.length === 0) {
      throw new Error('Invalid demo status: READY contract');
    }
  }
  if (enabled && (state === 'RESETTING' || state === 'VERIFYING')
      && (!stateChangedAt || !candidateGeneration || !writeLocked)) {
    throw new Error('Invalid demo status: lifecycle contract');
  }
  if (enabled && state === 'FAILED'
      && (!stateChangedAt || !writeLocked)) {
    throw new Error('Invalid demo status: FAILED contract');
  }
  if (enabled && lastResetAt && stateChangedAt
      && Date.parse(lastResetAt) > Date.parse(stateChangedAt)) {
    throw new Error('Invalid demo status: state transition order');
  }
  if (enabled && lastResetAt && nextResetAt
      && Date.parse(lastResetAt) >= Date.parse(nextResetAt)) {
    throw new Error('Invalid demo status: reset schedule order');
  }
  if (!enabled && state !== 'READY') {
    throw new Error('Invalid demo status: disabled state');
  }
  if (!enabled && (environmentName !== 'PRODUCTION'
      || generation !== null
      || stateChangedAt !== null
      || (candidateGeneration !== undefined && candidateGeneration !== null)
      || lastResetAt !== null
      || nextResetAt !== null
      || writeLocked
      || notice !== ''
      || !uploadEnabled
      || simulatedLocation !== undefined
      || publicAccounts.length !== 0)) {
    throw new Error('Invalid demo status: disabled contract');
  }

  return {
    enabled,
    environmentName,
    state: state as DemoServerState,
    stateChangedAt,
    generation,
    candidateGeneration,
    lastResetAt,
    nextResetAt,
    warningBeforeSeconds,
    writeLockBeforeSeconds,
    writeLocked,
    notice,
    uploadEnabled,
    simulatedLocation,
    publicAccounts,
  };
}
