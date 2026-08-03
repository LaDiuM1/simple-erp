import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import { useGetSuppliersQuery } from '@/features/reference/api/referenceApi';
import { eligibleSupplierOptions } from './eligibleSupplierOptions';

interface Props {
  value: unknown;
  onChange: (value: unknown) => void;
  mode: 'create' | 'edit';
  disabled: boolean;
}

export default function ProductSupplierField({ value, onChange, mode, disabled }: Props) {
  const { data = [] } = useGetSuppliersQuery();
  const supplierId = typeof value === 'string' && value !== '' ? Number(value) : null;
  const options = eligibleSupplierOptions(data, mode === 'edit' ? supplierId : null);

  return (
    <TextField
      select
      fullWidth
      size="small"
      label="공급사"
      required
      value={supplierId == null ? '' : String(supplierId)}
      onChange={(event) => onChange(event.target.value)}
      disabled={disabled}
    >
      {options.map((supplier) => (
        <MenuItem key={supplier.id} value={String(supplier.id)}>
          {supplier.nameKo ? `${supplier.name} (${supplier.nameKo})` : supplier.name}
        </MenuItem>
      ))}
    </TextField>
  );
}
