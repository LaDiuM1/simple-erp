import { useState } from 'react';
import EmployeeSelectField from '@/features/employee/components/EmployeeSelectField/EmployeeSelectField';

interface Props {
  value: unknown;
  onChange: (value: unknown) => void;
  /** 필터 라벨 — 소비 도메인의 의미로 지정 (예: 계약자). 기본 '직원'. */
  label?: string;
}

/**
 * 직원 선택 — GenericList custom 필터 어댑터 (근태 현황 / 계약 목록 등 공용).
 * EmployeeSelectField 는 표시 라벨을 외부 보유 요구 → 선택 시점의 이름을 로컬 state 로 유지.
 * 필터 초기화로 value 가 null 이 되면 라벨도 함께 비운다.
 * dense 톤으로 다른 필터 컨트롤과 박스 크기를 맞추고, 폭은 FilterSlot 이 통일한다.
 */
export default function EmployeeSelectFilter({ value, onChange, label = '직원' }: Props) {
  const [name, setName] = useState('');
  const id = typeof value === 'number' ? String(value) : '';

  return (
    <EmployeeSelectField
      dense
      label={label}
      value={id}
      valueLabel={id ? name : ''}
      onChange={(nextId, nextName) => {
        setName(nextName);
        onChange(nextId === '' ? null : Number(nextId));
      }}
    />
  );
}
