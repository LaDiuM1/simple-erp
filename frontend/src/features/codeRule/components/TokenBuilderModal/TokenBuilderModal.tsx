import { useEffect, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import RadioGroup from '@mui/material/RadioGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Radio from '@mui/material/Radio';
import TextField from '@mui/material/TextField';
import type { CodeRuleAttributeDescriptor } from '@/features/codeRule/types';
import {
  KindBlock,
  KindHint,
  KindLabel,
  Section,
  SectionTitle,
} from './TokenBuilderModal.styles';

export type TokenKind = 'userInput' | 'attribute';

interface Props {
  open: boolean;
  /** 도메인이 등록한 attribute 후보 — 비어있으면 분류값 종류 안 보임 */
  attributes: CodeRuleAttributeDescriptor[];
  onClose: () => void;
  /** 사용자 입력값 추가 — 토큰 카드의 사용자 입력값 chip 으로 누적 */
  onAddLiteral: (literal: string) => void;
  /** 분류값 종류 선택 — 두번째 모달 (AttributeMappingDialog) 호출. 첫 모달은 닫음. */
  onSelectAttribute: () => void;
}

/**
 * 토큰 만들기 — 카드에 추가할 사용자 토큰 종류를 선택.
 * <p>
 * 기본 토큰 (날짜 / 시퀀스 / 부모) 은 카드에 이미 깔려 있으므로 본 모달에서는 제공하지 않음.
 */
export default function TokenBuilderModal({
  open,
  attributes,
  onClose,
  onAddLiteral,
  onSelectAttribute,
}: Props) {
  const [kind, setKind] = useState<TokenKind>('userInput');
  const [literal, setLiteral] = useState('');

  useEffect(() => {
    if (!open) return;
    setKind('userInput');
    setLiteral('');
  }, [open]);

  const handleConfirm = () => {
    if (kind === 'userInput') {
      const trimmed = literal.trim();
      if (!trimmed) return;
      onAddLiteral(trimmed);
    } else if (kind === 'attribute') {
      onSelectAttribute();
    }
  };

  const canConfirm =
    (kind === 'userInput' && literal.trim().length > 0)
    || kind === 'attribute';

  const confirmLabel = kind === 'attribute' ? '다음' : '추가';

  const hasAttributes = attributes.length > 0;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xs"
      fullWidth
      slotProps={{ paper: { sx: { borderRadius: 0 } } }}
    >
      <DialogTitle sx={{ fontSize: '1rem', fontWeight: 700, py: 1.5 }}>
        토큰 만들기
      </DialogTitle>
      <DialogContent dividers>
        <Section>
          <SectionTitle>종류</SectionTitle>
          <RadioGroup
            value={kind}
            onChange={(e) => setKind(e.target.value as TokenKind)}
          >
            <KindBlock>
              <FormControlLabel
                value="userInput"
                control={<Radio size="small" />}
                label={<KindLabel>사용자 입력값</KindLabel>}
              />
              <KindHint>
                임의 문자열 (예: D, EMP-, /) 을 토큰 카드에 추가. 카드에서 패턴으로 드래그 또는 클릭으로 삽입.
              </KindHint>
            </KindBlock>
            {hasAttributes && (
              <KindBlock>
                <FormControlLabel
                  value="attribute"
                  control={<Radio size="small" />}
                  label={<KindLabel>분류값</KindLabel>}
                />
                <KindHint>
                  도메인 분류 (예: 고객사 분류) 와 코드값 매핑을 정의. 분류별로 시퀀스 분리.
                </KindHint>
              </KindBlock>
            )}
          </RadioGroup>
        </Section>

        {kind === 'userInput' && (
          <Section>
            <SectionTitle>입력값</SectionTitle>
            <TextField
              variant="outlined"
              size="small"
              fullWidth
              autoFocus
              value={literal}
              onChange={(e) => setLiteral(e.target.value)}
              placeholder="예: D, EMP-, /"
              slotProps={{ htmlInput: { maxLength: 50 } }}
              helperText="이 값이 코드의 해당 위치에 그대로 들어갑니다."
            />
          </Section>
        )}
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
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
