import type { ReactNode } from 'react';
import {
  KpiBody,
  KpiIcon,
  KpiLabel,
  KpiRoot,
  KpiSuffix,
  KpiUnit,
  KpiValue,
  KpiValueRow,
} from './KpiCard.styles';

interface Props {
  label: string;
  value: number;
  unit?: string;
  suffix?: string;
  icon: ReactNode;
  onClick?: () => void;
}

export default function KpiCard({ label, value, unit = '건', suffix, icon, onClick }: Props) {
  return (
    <KpiRoot type="button" onClick={onClick}>
      <KpiIcon>{icon}</KpiIcon>
      <KpiBody>
        <KpiLabel>{label}</KpiLabel>
        <KpiValueRow>
          <KpiValue>{value.toLocaleString('ko-KR')}</KpiValue>
          <KpiUnit>{unit}</KpiUnit>
        </KpiValueRow>
        {suffix && <KpiSuffix>{suffix}</KpiSuffix>}
      </KpiBody>
    </KpiRoot>
  );
}
