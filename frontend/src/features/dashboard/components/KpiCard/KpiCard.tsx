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
  onClick?: () => void;
}

export default function KpiCard({ label, value, unit = '건', suffix, icon, onClick }: Props) {
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
      {onClick && (
        <KpiArrow aria-hidden="true">
          <ArrowForwardRoundedIcon />
        </KpiArrow>
      )}
    </>
  );

  if (!onClick) return <KpiRoot>{content}</KpiRoot>;

  return (
    <KpiButton
      type="button"
      onClick={onClick}
      aria-label={`${label} ${value.toLocaleString('ko-KR')}${unit} 보기`}
    >
      {content}
    </KpiButton>
  );
}
