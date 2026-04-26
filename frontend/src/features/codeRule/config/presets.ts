/**
 * 코드 채번 규칙의 프리셋 정의.
 * <p>
 * 비개발자가 토큰 문법 (`{PREFIX}`, `{SEQ:4}`) 을 직접 다루지 않고도
 * 흔한 코드 형식을 카드 1번 클릭으로 적용할 수 있도록 미리 만든 5종.
 * 프리셋 외 자유 형식이 필요하면 `custom` 카드 → 기존 토큰 빌더 사용.
 */

import { INPUT_MODE, RESET_POLICY, type InputMode, type ResetPolicy } from '@/features/codeRule/types';

export type PresetId = 'simple' | 'yearly' | 'monthly' | 'daily' | 'parent' | 'custom';

export interface Preset {
  id: PresetId;
  label: string;
  /** 카드 본문 1줄 설명 — 비개발자 톤 */
  description: string;
  /** 카드 예시 코드 */
  example: string;
  /** prefix 입력 노출 여부 (parent 는 부모 코드를 사용하므로 false) */
  hasPrefix: boolean;
  /** seqLen 슬라이더 노출 여부 (custom 은 토큰 빌더에서 결정) */
  hasSeqLen: boolean;
  /** 프리셋 → 패턴/정책 빌드 */
  build: (prefix: string, seqLen: number) => {
    pattern: string;
    resetPolicy: ResetPolicy;
    parentScoped: boolean;
  };
}

export const PRESETS: Preset[] = [
  {
    id: 'simple',
    label: '단순 순번',
    description: '앞글자 + 일련번호. 회사 전체에서 1번부터 차례로 매겨집니다.',
    example: 'D001, D002, D003',
    hasPrefix: true,
    hasSeqLen: true,
    build: (_prefix, seqLen) => ({
      pattern: `{PREFIX}{SEQ:${seqLen}}`,
      resetPolicy: RESET_POLICY.NEVER,
      parentScoped: false,
    }),
  },
  {
    id: 'yearly',
    label: '연도 + 순번',
    description: '연도를 포함하고 매년 1번부터 다시 시작합니다.',
    example: 'D2026-001, D2026-002',
    hasPrefix: true,
    hasSeqLen: true,
    build: (_prefix, seqLen) => ({
      pattern: `{PREFIX}{YYYY}-{SEQ:${seqLen}}`,
      resetPolicy: RESET_POLICY.YEARLY,
      parentScoped: false,
    }),
  },
  {
    id: 'monthly',
    label: '월별 순번',
    description: '연월을 포함하고 매월 1번부터 다시 시작합니다.',
    example: 'D2604-001, D2604-002',
    hasPrefix: true,
    hasSeqLen: true,
    build: (_prefix, seqLen) => ({
      pattern: `{PREFIX}{YY}{MM}-{SEQ:${seqLen}}`,
      resetPolicy: RESET_POLICY.MONTHLY,
      parentScoped: false,
    }),
  },
  {
    id: 'daily',
    label: '일자별 순번',
    description: '연월일을 포함하고 매일 1번부터 다시 시작합니다.',
    example: 'D260426-001, D260426-002',
    hasPrefix: true,
    hasSeqLen: true,
    build: (_prefix, seqLen) => ({
      pattern: `{PREFIX}{YY}{MM}{DD}-{SEQ:${seqLen}}`,
      resetPolicy: RESET_POLICY.DAILY,
      parentScoped: false,
    }),
  },
  {
    id: 'parent',
    label: '부모 + 순번',
    description: '부모 코드 뒤에 순번을 붙이고 부모별로 1번부터 다시 시작합니다.',
    example: 'D001-001, D001-002, D002-001',
    hasPrefix: false,
    hasSeqLen: true,
    build: (_prefix, seqLen) => ({
      pattern: `{PARENT}-{SEQ:${seqLen}}`,
      resetPolicy: RESET_POLICY.NEVER,
      parentScoped: true,
    }),
  },
  {
    id: 'custom',
    label: '직접 만들기',
    description: '토큰을 직접 조합해 자유로운 형식을 만듭니다 (고급).',
    example: '{PREFIX}-{YY}{MM}-{SEQ:4}',
    hasPrefix: true,
    hasSeqLen: false,
    build: (_prefix, seqLen) => ({
      // 사용자가 토큰 빌더로 자유 편집할 시작 패턴
      pattern: `{PREFIX}{SEQ:${seqLen}}`,
      resetPolicy: RESET_POLICY.NEVER,
      parentScoped: false,
    }),
  },
];

export const PRESET_BY_ID: Record<PresetId, Preset> = PRESETS.reduce(
  (acc, p) => {
    acc[p.id] = p;
    return acc;
  },
  {} as Record<PresetId, Preset>,
);

export function detectPreset(args: {
  pattern: string;
  resetPolicy: ResetPolicy;
  parentScoped: boolean;
}): PresetId {
  const p = args.pattern.trim();
  if (
    /^\{PREFIX\}\{SEQ:\d+\}$/.test(p)
    && args.resetPolicy === RESET_POLICY.NEVER
    && !args.parentScoped
  ) {
    return 'simple';
  }
  if (
    /^\{PREFIX\}\{YYYY\}-\{SEQ:\d+\}$/.test(p)
    && args.resetPolicy === RESET_POLICY.YEARLY
    && !args.parentScoped
  ) {
    return 'yearly';
  }
  if (
    /^\{PREFIX\}\{YY\}\{MM\}-\{SEQ:\d+\}$/.test(p)
    && args.resetPolicy === RESET_POLICY.MONTHLY
    && !args.parentScoped
  ) {
    return 'monthly';
  }
  if (
    /^\{PREFIX\}\{YY\}\{MM\}\{DD\}-\{SEQ:\d+\}$/.test(p)
    && args.resetPolicy === RESET_POLICY.DAILY
    && !args.parentScoped
  ) {
    return 'daily';
  }
  if (
    /^\{PARENT\}-\{SEQ:\d+\}$/.test(p)
    && args.resetPolicy === RESET_POLICY.NEVER
    && args.parentScoped
  ) {
    return 'parent';
  }
  return 'custom';
}

/** 패턴에서 SEQ 자릿수 추출. 못 찾으면 null. */
export function extractSeqLen(pattern: string): number | null {
  const m = pattern.match(/\{SEQ:(\d+)\}/);
  return m ? Number(m[1]) : null;
}

export function summarize(args: {
  preset: PresetId;
  prefix: string;
  seqLen: number;
  pattern: string;
  inputMode: InputMode;
}): string {
  const seqDesc = `순번 ${args.seqLen}자리`;
  const prefix = args.prefix.trim();
  const prefixDesc = prefix ? `앞글자 "${prefix}"` : '앞글자 없음';
  const inputDesc = args.inputMode === INPUT_MODE.MANUAL
    ? ' 코드는 사용자가 직접 입력합니다.'
    : args.inputMode === INPUT_MODE.AUTO_OR_MANUAL
      ? ' 시스템 자동 부여 + 사용자 직접 입력 둘 다 허용합니다.'
      : ' 시스템이 자동으로 부여합니다.';

  switch (args.preset) {
    case 'simple':
      return `${prefixDesc} + ${seqDesc}. 회사 전체에서 한 번씩 매겨집니다.${inputDesc}`;
    case 'yearly':
      return `${prefixDesc} + 연도 4자리 + ${seqDesc}. 매년 1번부터 다시 시작합니다.${inputDesc}`;
    case 'monthly':
      return `${prefixDesc} + 연월 4자리 + ${seqDesc}. 매월 1번부터 다시 시작합니다.${inputDesc}`;
    case 'daily':
      return `${prefixDesc} + 연월일 6자리 + ${seqDesc}. 매일 1번부터 다시 시작합니다.${inputDesc}`;
    case 'parent':
      return `부모 코드 + ${seqDesc}. 부모별로 1번부터 다시 시작합니다.${inputDesc}`;
    case 'custom':
      return `직접 만든 형식: ${args.pattern}${inputDesc}`;
  }
}
