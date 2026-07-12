import { useState } from 'react';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField';

interface Props {
  value: unknown;
  onChange: (value: unknown) => void;
  label?: string;
}

/**
 * 고객사 선택 — GenericList custom 필터 어댑터 (EmployeeSelectFilter 와 동일 패턴).
 * 선택 시점의 이름을 로컬 state 로 유지, 필터 초기화 시 라벨도 함께 비운다.
 * dense 톤으로 다른 필터 컨트롤과 박스 크기를 맞추고, 폭은 FilterSlot 이 통일한다.
 */
export default function CustomerSelectFilter({ value, onChange, label = '고객사' }: Props) {
  const [name, setName] = useState('');
  const id = typeof value === 'number' ? String(value) : '';

  return (
    <CustomerSelectField
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
