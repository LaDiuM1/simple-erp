import type { ReactNode } from 'react';
import { useEffect, useRef } from 'react';
import Dialog from '@mui/material/Dialog';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import Muted from '@/shared/ui/atoms/Muted';
import {
  FieldLabelCell,
  FieldValueCell,
  FieldsTable,
  ModalCloseButton,
  ModalHeader,
  ModalTitle,
} from './GenericDetailModal.styles';

export interface DetailModalField {
  label: string;
  value: ReactNode;
}

interface Props {
  open: boolean;
  onClose: () => void;
  title: ReactNode;
  fields: DetailModalField[];
  /** 모달 최대 폭 (기본 560). 본문 가변 너비에서 wrap 됨. */
  maxWidth?: number | string;
}

/**
 * 행/항목의 모든 필드를 [label][value] 매트릭스로 보여주는 공용 모달.
 * GenericTabbedTable 의 truncated 셀에서 행 클릭 시 풀 컨텐츠 출력 등에 사용.
 * value 는 셀 폭 안에서 자동 wrap, 항목이 많으면 본문이 세로 스크롤.
 *
 * **Exit transition 안정성**: consumer 가 보통 `target ? buildFields(target) : []` 패턴으로
 * fields 를 만드는데, `setTarget(null)` 시 open=false 와 동시에 fields 가 [] 로 비워져
 * MUI Dialog 의 fade-out 중에 본문이 collapse 되는 것이 보임. 이를 방지하기 위해
 * 모달 내부에서 open=true 일 때의 마지막 props 를 ref 에 캡처하고, 닫히는 동안에는
 * 그 snapshot 을 렌더한다 — consumer 는 단순한 패턴을 그대로 유지.
 */
export default function GenericDetailModal({
  open,
  onClose,
  title,
  fields,
  maxWidth = 560,
}: Props) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  const snapshotRef = useRef<{ title: ReactNode; fields: DetailModalField[] }>({
    title,
    fields,
  });
  if (open) {
    snapshotRef.current = { title, fields };
  }
  const display = open ? { title, fields } : snapshotRef.current;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      slotProps={{
        backdrop: { sx: { backgroundColor: 'rgb(0 0 0 / 0.4)' } },
      }}
      PaperProps={{
        sx: (theme) => ({
          borderRadius: '6px',
          border: `1px solid ${theme.palette.divider}`,
          boxShadow: theme.shadows[4],
          width: '100%',
          maxWidth,
          maxHeight: '80vh',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }),
      }}
    >
      <ModalHeader>
        <ModalTitle>{display.title}</ModalTitle>
        <ModalCloseButton size="small" onClick={onClose} aria-label="닫기">
          <CloseRoundedIcon fontSize="small" />
        </ModalCloseButton>
      </ModalHeader>
      <FieldsTable>
        {display.fields.map((f, i) => {
          const isEmpty = f.value === null || f.value === undefined || f.value === '';
          return (
            <FieldsRow key={`${f.label}-${i}`} field={f} isEmpty={isEmpty} />
          );
        })}
      </FieldsTable>
    </Dialog>
  );
}

function FieldsRow({ field, isEmpty }: { field: DetailModalField; isEmpty: boolean }) {
  return (
    <>
      <FieldLabelCell>{field.label}</FieldLabelCell>
      <FieldValueCell>{isEmpty ? <Muted /> : field.value}</FieldValueCell>
    </>
  );
}
