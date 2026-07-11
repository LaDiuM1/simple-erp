import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import type { ApprovalFormValues } from '@/features/approval/types';

/** BE ApprovalCreateRequest 의 @NotBlank title 미러 — 즉시 피드백용. */
export const approvalValidators: ValidatorMap<ApprovalFormValues> = {
  title: (v) => (v.trim() === '' ? '제목을 입력해주세요.' : null),
};
