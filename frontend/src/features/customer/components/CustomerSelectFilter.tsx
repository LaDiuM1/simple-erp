import { useState } from 'react';
import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField';

interface Props {
  value: unknown;
  onChange: (value: unknown) => void;
  label?: string;
}

/**
 * 고객사 선택 — GenericList custom 필터 어댑터 (EmployeeSelectFilter 와 동일 패턴).
 * 선택 시점의 이름을 로컬 state 로 유지, 필터 초기화 시 라벨도 함께 비운다.
 */
export default function CustomerSelectFilter({ value, onChange, label = '고객사' }: Props) {
  const [name, setName] = useState('');
  const id = typeof value === 'number' ? String(value) : '';

  return (
    <FieldBox>
      <CustomerSelectField
        label={label}
        value={id}
        valueLabel={id ? name : ''}
        onChange={(nextId, nextName) => {
          setName(nextName);
          onChange(nextId === '' ? null : Number(nextId));
        }}
        placeholder={`${label} 검색`}
      />
    </FieldBox>
  );
}

const FieldBox = styled(Box)({
  width: 200,
});
