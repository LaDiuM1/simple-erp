import type { ReactNode } from 'react';
import Muted from '@/shared/ui/atoms/Muted';
import {
  HeaderDetailsRoot,
  HeaderDetailsTable,
  HeaderLabelCell,
  HeaderValueCell,
} from './GenericHeaderDetails.styles';

export interface HeaderDetailField {
  label: string;
  value: ReactNode | null | undefined;
  /** true 이면 한 줄 전체 폭 차지 (label 한 칸 + value 가 나머지). 비고/주소 같은 긴 텍스트에 적합. */
  fullWidth?: boolean;
}

interface Props {
  fields: HeaderDetailField[];
  /** 한 줄에 들어가는 label-value 쌍 수 (기본 2). md 이상에서 적용, 그 이하는 항상 1쌍. */
  columns?: number;
}

/**
 * 도메인 상세 페이지 상단의 기본 정보 카드 — 직각 배열 테이블 형식 + sticky 최상단 고정.
 * label cell 은 옅은 배경, value cell 은 일반 배경. fullWidth 필드는 한 줄 전체 차지.
 */
export default function GenericHeaderDetails({ fields, columns = 2 }: Props) {
  return (
    <HeaderDetailsRoot>
      <HeaderDetailsTable columns={columns}>
        {fields.map((f, i) => (
          <Cell key={`${f.label}-${i}`} field={f} />
        ))}
      </HeaderDetailsTable>
    </HeaderDetailsRoot>
  );
}

function Cell({ field }: { field: HeaderDetailField }) {
  const isEmpty = field.value === null || field.value === undefined || field.value === '';
  return (
    <>
      <HeaderLabelCell sx={field.fullWidth ? { gridColumn: '1' } : undefined}>
        {field.label}
      </HeaderLabelCell>
      <HeaderValueCell sx={field.fullWidth ? { gridColumn: '2 / -1' } : undefined}>
        {isEmpty ? <Muted /> : field.value}
      </HeaderValueCell>
    </>
  );
}
