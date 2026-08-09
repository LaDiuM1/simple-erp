import type { ReactNode } from 'react';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import {
  KpiArrow,
  KpiBody,
  KpiButton,
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
  to?: string;
}

export default function KpiCard({ label, value, unit = '건', suffix, icon, to }: Props) {
  const content = (
    <>
      <KpiIcon>{icon}</KpiIcon>
      <KpiBody>
        <KpiLabel>{label}</KpiLabel>
        <KpiValueRow>
          <KpiValue>{value.toLocaleString('ko-KR')}</KpiValue>
          <KpiUnit>{unit}</KpiUnit>
        </KpiValueRow>
        {suffix && <KpiSuffix>{suffix}</KpiSuffix>}
      </KpiBody>
      {to && (
        <KpiArrow aria-hidden="true">
          <ArrowForwardRoundedIcon />
        </KpiArrow>
      )}
    </>
  );

  if (!to) return <KpiRoot>{content}</KpiRoot>;

  return (
    <KpiButton
      to={to}
      aria-label={`${label} ${value.toLocaleString('ko-KR')}${unit} 보기`}
    >
      {content}
    </KpiButton>
  );
}
