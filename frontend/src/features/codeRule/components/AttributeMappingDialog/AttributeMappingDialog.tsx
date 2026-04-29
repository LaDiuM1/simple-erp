import { useEffect, useMemo, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import type {
  CodeRuleAttributeDescriptor,
  CodeRuleAttributeMapping,
} from '@/features/codeRule/types';
import { Field, FieldLabel, Section } from './AttributeMappingDialog.styles';

interface Props {
  open: boolean;
  attributes: CodeRuleAttributeDescriptor[];
  /** 이미 정의된 매핑들 — 분류 항목 select 의 disabled 표시 용 */
  existingMappings: CodeRuleAttributeMapping[];
  onClose: () => void;
  onConfirm: (mapping: CodeRuleAttributeMapping) => void;
}

const MONOSPACE = 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';

/**
 * 분류값 토큰 매핑 입력 — 토큰 만들기 모달의 분류값 선택 시 한번 더 띄워지는 두번째 모달.
 * <p>
 * 분류 종류 / 분류 항목 / 설정할 값 3가지를 세로로 입력. 완료 시 매핑 1건 + (필요 시) 패턴에 {KEY} 토큰 추가.
 */
export default function AttributeMappingDialog({
  open,
  attributes,
  existingMappings,
  onClose,
  onConfirm,
}: Props) {
  const [attrKey, setAttrKey] = useState<string>('');
  const [sourceValue, setSourceValue] = useState<string>('');
  const [codeValue, setCodeValue] = useState<string>('');

  useEffect(() => {
    if (!open) return;
    setAttrKey(attributes[0]?.key ?? '');
    setSourceValue('');
    setCodeValue('');
  }, [open, attributes]);

  const descriptor = useMemo(
    () => attributes.find((a) => a.key === attrKey),
    [attributes, attrKey],
  );

  const canConfirm =
    attrKey !== ''
    && sourceValue !== ''
    && codeValue.trim() !== '';

  const handleConfirm = () => {
    if (!canConfirm) return;
    onConfirm({ attributeKey: attrKey, sourceValue, codeValue: codeValue.trim() });
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xs"
      fullWidth
      slotProps={{ paper: { sx: { borderRadius: 0 } } }}
    >
      <DialogTitle sx={{ fontSize: '1rem', fontWeight: 700, py: 1.5 }}>
        분류값 매핑 추가
      </DialogTitle>
      <DialogContent dividers>
        <Section>
          <Field>
            <FieldLabel>분류 종류</FieldLabel>
            <TextField
              select
              variant="outlined"
              size="small"
              fullWidth
              value={attrKey}
              onChange={(e) => {
                setAttrKey(e.target.value);
                setSourceValue('');
              }}
            >
              {attributes.map((a) => (
                <MenuItem key={a.key} value={a.key}>
                  {a.label}
                  <span style={{ marginLeft: 8, color: '#888', fontFamily: MONOSPACE, fontSize: '0.75rem' }}>
                    {`{${a.key}}`}
                  </span>
                </MenuItem>
              ))}
            </TextField>
          </Field>

          <Field>
            <FieldLabel>분류 항목</FieldLabel>
            <TextField
              select
              variant="outlined"
              size="small"
              fullWidth
              value={sourceValue}
              onChange={(e) => setSourceValue(e.target.value)}
              disabled={!descriptor}
            >
              <MenuItem value="">선택</MenuItem>
              {descriptor?.values.map((v) => {
                const isAlreadyMapped = existingMappings.some(
                  (m) => m.attributeKey === attrKey && m.sourceValue === v.value && m.codeValue.trim() !== '',
                );
                return (
                  <MenuItem key={v.value} value={v.value}>
                    {v.label}
                    {isAlreadyMapped && (
                      <span style={{ marginLeft: 8, color: '#888', fontSize: '0.7rem' }}>
                        (이미 매핑됨 — 덮어쓰기)
                      </span>
                    )}
                  </MenuItem>
                );
              })}
            </TextField>
          </Field>

          <Field>
            <FieldLabel>설정할 값</FieldLabel>
            <TextField
              variant="outlined"
              size="small"
              fullWidth
              value={codeValue}
              onChange={(e) => setCodeValue(e.target.value)}
              placeholder="예: G"
              slotProps={{
                input: { sx: { fontFamily: MONOSPACE, fontSize: '0.875rem' } },
                htmlInput: { maxLength: 50 },
              }}
              helperText="이 값이 코드 안 분류 토큰 위치에 치환됩니다."
            />
          </Field>
        </Section>
      </DialogContent>
      <DialogActions sx={{ px: 2, py: 1.5 }}>
        <Button onClick={onClose} size="small" sx={{ textTransform: 'none' }}>
          취소
        </Button>
        <Button
          onClick={handleConfirm}
          disabled={!canConfirm}
          variant="contained"
          size="small"
          disableElevation
          sx={{ textTransform: 'none', borderRadius: 0 }}
        >
          추가
        </Button>
      </DialogActions>
    </Dialog>
  );
}
