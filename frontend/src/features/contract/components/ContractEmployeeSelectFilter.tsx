import * as React from 'react';
import ContractEmployeeSelectField from './ContractEmployeeSelectField';

interface Props {
  value: unknown;
  onChange: (value: unknown) => void;
}

export default function ContractEmployeeSelectFilter({ value, onChange }: Props) {
  const [label, setLabel] = React.useState('');
  const selectedId = value == null || value === '' ? '' : String(value);

  return (
    <ContractEmployeeSelectField
      activeOnly={false}
      dense
      value={selectedId}
      valueLabel={selectedId === '' ? '' : label}
      onChange={(id, name) => {
        setLabel(name);
        onChange(id === '' ? null : Number(id));
      }}
      placeholder="계약자"
    />
  );
}
