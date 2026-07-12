import { useState } from 'react';
import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
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
 */
export default function EmployeeSelectFilter({ value, onChange, label = '직원' }: Props) {
  const [name, setName] = useState('');
  const id = typeof value === 'number' ? String(value) : '';

  return (
    <FieldBox>
      <EmployeeSelectField
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
