import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import { validateContentText } from '@/shared/validation/contentText';
import type { BoardFormValues } from '@/features/board/types';

/** BE PostCreateRequest / PostUpdateRequest 의 Bean Validation (@NotBlank, @Size(max = 200)) 미러. */
export const boardValidators: ValidatorMap<BoardFormValues> = {
  title: (v) => {
    if (v.trim() === '') return '제목을 입력해주세요.';
    if (v.trim().length > 200) return '제목은 200자 이하로 입력해주세요.';
    return null;
  },
  content: (v) => validateContentText(v, '내용', true),
};
