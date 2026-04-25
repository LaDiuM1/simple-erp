import type { ReactNode } from 'react';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import { FieldItem } from './FormField.styles';
import type {
  CustomFieldConfig,
  DateFieldConfig,
  EmailFieldConfig,
  FieldConfig,
  FieldOption,
  PasswordFieldConfig,
  PhoneFieldConfig,
  SelectFieldConfig,
  TextFieldConfig,
} from './types';

interface Props<TValues extends object> {
  field: FieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  /** 입력 불가 상태 — disabledOnEdit 플래그를 GenericForm 이 mode 와 합쳐 결정 */
  disabled?: boolean;
}

/**
 * FieldConfig 의 type 에 따라 적절한 입력 컴포넌트를 렌더.
 * FieldItem wrapper 가 grid column span (fullWidth) 을 담당.
 */
export default function FormField<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: Props<TValues>) {
  return (
    <FieldItem fullWidth={field.fullWidth}>
      <FieldBody field={field} value={value} onChange={onChange} disabled={disabled} />
    </FieldItem>
  );
}

function FieldBody<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: Props<TValues>): ReactNode {
  switch (field.type) {
    case 'text':
      return <TextFieldRenderer field={field} value={value} onChange={onChange} disabled={disabled} />;
    case 'password':
      return <PasswordFieldRenderer field={field} value={value} onChange={onChange} disabled={disabled} />;
    case 'email':
      return <EmailFieldRenderer field={field} value={value} onChange={onChange} disabled={disabled} />;
    case 'phone':
      return <PhoneFieldRenderer field={field} value={value} onChange={onChange} disabled={disabled} />;
    case 'date':
      return <DateFieldRenderer field={field} value={value} onChange={onChange} disabled={disabled} />;
    case 'select':
      return <SelectFieldRenderer field={field} value={value} onChange={onChange} disabled={disabled} />;
    case 'custom':
      return <CustomFieldRenderer field={field} value={value} onChange={onChange} />;
  }
}

/* --------------------------------------------------------------------------
 * Text-like renderers (TextField 기반)
 * ------------------------------------------------------------------------ */

function TextFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: {
  field: TextFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  return (
    <TextField
      fullWidth
      size="small"
      label={field.label}
      value={toStringValue(value)}
      onChange={(e) => onChange(e.target.value)}
      required={field.required}
      helperText={field.helperText}
      placeholder={field.placeholder}
      disabled={disabled}
      slotProps={{
        htmlInput: { maxLength: field.maxLength },
      }}
    />
  );
}

function PasswordFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: {
  field: PasswordFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  return (
    <TextField
      fullWidth
      size="small"
      type="password"
      label={field.label}
      value={toStringValue(value)}
      onChange={(e) => onChange(e.target.value)}
      required={field.required}
      helperText={field.helperText}
      placeholder={field.placeholder}
      disabled={disabled}
      slotProps={{
        htmlInput: { minLength: field.minLength },
      }}
    />
  );
}

function EmailFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: {
  field: EmailFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  return (
    <TextField
      fullWidth
      size="small"
      type="email"
      label={field.label}
      value={toStringValue(value)}
      onChange={(e) => onChange(e.target.value)}
      required={field.required}
      helperText={field.helperText}
      placeholder={field.placeholder}
      disabled={disabled}
    />
  );
}

function PhoneFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: {
  field: PhoneFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  return (
    <TextField
      fullWidth
      size="small"
      type="tel"
      label={field.label}
      value={toStringValue(value)}
      onChange={(e) => onChange(e.target.value)}
      helperText={field.helperText}
      placeholder={field.placeholder}
      disabled={disabled}
    />
  );
}

function DateFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: {
  field: DateFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  return (
    <TextField
      fullWidth
      size="small"
      type="date"
      label={field.label}
      value={toStringValue(value)}
      onChange={(e) => onChange(e.target.value)}
      required={field.required}
      helperText={field.helperText}
      disabled={disabled}
      slotProps={{
        inputLabel: { shrink: true },
      }}
    />
  );
}

/* --------------------------------------------------------------------------
 * Select (정적 / 동적 옵션 지원)
 * ------------------------------------------------------------------------ */

function SelectFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
  disabled,
}: {
  field: SelectFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  if (field.useOptions) {
    return (
      <DynamicSelectRenderer
        useOptions={field.useOptions}
        mapOptions={field.mapOptions}
        fallbackOptions={field.options ?? []}
        field={field}
        value={value}
        onChange={onChange}
        disabled={disabled}
      />
    );
  }
  return (
    <StaticSelect
      field={field}
      value={value}
      onChange={onChange}
      options={field.options ?? []}
      disabled={disabled}
    />
  );
}

function DynamicSelectRenderer<TValues extends object>({
  useOptions,
  mapOptions,
  fallbackOptions,
  field,
  value,
  onChange,
  disabled,
}: {
  useOptions: () => { data?: unknown };
  mapOptions?: (data: unknown) => FieldOption[];
  fallbackOptions: FieldOption[];
  field: SelectFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  disabled?: boolean;
}) {
  const { data } = useOptions();
  const options: FieldOption[] =
    data != null && mapOptions ? mapOptions(data) : fallbackOptions;
  return <StaticSelect field={field} value={value} onChange={onChange} options={options} disabled={disabled} />;
}

function StaticSelect<TValues extends object>({
  field,
  value,
  onChange,
  options,
  disabled,
}: {
  field: SelectFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
  options: FieldOption[];
  disabled?: boolean;
}) {
  return (
    <TextField
      select
      fullWidth
      size="small"
      label={field.label}
      value={toStringValue(value)}
      onChange={(e) => onChange(e.target.value)}
      required={field.required}
      helperText={field.helperText}
      disabled={disabled}
    >
      {!field.required && <MenuItem value="">-</MenuItem>}
      {options.map((o) => (
        <MenuItem key={String(o.value)} value={String(o.value)}>
          {o.label}
        </MenuItem>
      ))}
    </TextField>
  );
}

/* --------------------------------------------------------------------------
 * Custom render
 * ------------------------------------------------------------------------ */

function CustomFieldRenderer<TValues extends object>({
  field,
  value,
  onChange,
}: {
  field: CustomFieldConfig<TValues>;
  value: unknown;
  onChange: (value: unknown) => void;
}): ReactNode {
  return <>{field.render({ value, onChange })}</>;
}

function toStringValue(v: unknown): string {
  if (v == null) return '';
  if (typeof v === 'string') return v;
  if (typeof v === 'number') return String(v);
  return '';
}
