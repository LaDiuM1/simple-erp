import { StatDelta, StatLabel, StatRoot, StatUnit, StatValue, StatValueRow } from './StatTile.styles';

interface Props {
  label: string;
  value: number;
  unit: string;
  /** 증감 수치 — null 이면 "변동 없음" 대신 미노출 */
  delta?: { value: number; periodLabel: string } | null;
  onClick: () => void;
}

/**
 * 컴팩트 집계 타일 — 기존 KpiCard 의 조연 강등판. 아이콘 박스 제거, 증감 맥락 추가.
 */
export default function StatTile({ label, value, unit, delta, onClick }: Props) {
  return (
    <StatRoot type="button" onClick={onClick}>
      <StatLabel>{label}</StatLabel>
      <StatValueRow>
        <StatValue>{value.toLocaleString()}</StatValue>
        <StatUnit>{unit}</StatUnit>
      </StatValueRow>
      {delta && delta.value > 0 && (
        <StatDelta>
          <b>▲ {delta.value.toLocaleString()}</b> {delta.periodLabel}
        </StatDelta>
      )}
    </StatRoot>
  );
}
