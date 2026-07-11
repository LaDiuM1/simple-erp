import type * as React from 'react';
import type { FieldValidation } from '@/shared/hooks/useFieldValidation';
import type { AttachedFile } from '@/shared/ui/FileAttachField';
import type { BoardCategory, BoardFormValues } from '@/features/board/types';

/**
 * 게시글 등록 / 수정 폼 상태 — 공용 Body (BoardForm) 의 props 타입.
 * 양쪽 모드의 상태 구조가 동일해 단일 인터페이스로 공유.
 * categoryOptions 는 NOTICE 작성 권한 (canWrite) 에 따라 각 hook 이 계산해 내려준다.
 */
export interface BoardFormState {
  values: BoardFormValues;
  update: <K extends keyof BoardFormValues>(key: K, v: BoardFormValues[K]) => void;
  /** FileAttachField 함수형 onChange 어댑트 — 업로드 완료 시점의 stale 스냅샷이 다른 입력을 덮지 않도록 prev 기반 반영. */
  updateAttachments: (update: (prev: AttachedFile[]) => AttachedFile[]) => void;
  validation: FieldValidation<BoardFormValues>;
  categoryOptions: { value: BoardCategory; label: string }[];
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}
