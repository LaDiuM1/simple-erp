import { MiniLabel, MiniRoot, MiniUnit, MiniValue, MiniValueRow } from './MiniStatTile.styles';

interface Props {
  label: string;
  value: string;
  unit: string;
  /** 유입 지표 (신규 등) 는 코발트로 긍정 강조 */
  accent?: boolean;
}

/** 벤토 우하단의 보조 지표 미니 타일 — 이번 주 신규 / 미접촉 고객 등. */
export default function MiniStatTile({ label, value, unit, accent }: Props) {
  return (
    <MiniRoot>
      <MiniLabel>{label}</MiniLabel>
      <MiniValueRow>
        <MiniValue accent={accent}>{value}</MiniValue>
        <MiniUnit>{unit}</MiniUnit>
      </MiniValueRow>
    </MiniRoot>
  );
}
