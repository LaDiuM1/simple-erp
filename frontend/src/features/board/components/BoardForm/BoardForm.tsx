import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import ArticleRoundedIcon from '@mui/icons-material/ArticleRounded';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import FileAttachField from '@/shared/ui/FileAttachField';
import { FormSection } from '@/shared/ui/GenericForm';
import type { BoardCategory } from '@/features/board/types';
import type { BoardFormState } from '@/features/board/hooks/boardFormState';
import { CategoryTitleGrid, CreateForm, CreateRoot, FieldColumn } from './boardForm.styles';

interface Props {
  form: BoardFormState;
  mode: 'create' | 'edit';
}

/**
 * 게시글 등록 / 수정 공용 Body — form-state hook 결과를 렌더만. fetch 미관여.
 * NOTICE 카테고리 노출 여부는 hook 이 categoryOptions 로 결정해 내려준다.
 */
export default function BoardForm({ form, mode }: Props) {
  const { values, update, validation } = form;
  const formId = mode === 'create' ? 'board-create-form' : 'board-edit-form';

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: mode === 'create' ? 'create' : 'save',
            formId,
            loading: form.isSaving,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={formId} onSubmit={form.handleSubmit} noValidate>
          <FormSection
            icon={<ArticleRoundedIcon sx={{ fontSize: 18 }} />}
            title="게시글 정보"
            description="카테고리 / 제목 / 본문 / 첨부 파일."
          >
            <FieldColumn>
              <CategoryTitleGrid>
                <TextField
                  select
                  size="small"
                  label="카테고리"
                  required
                  value={values.category}
                  onChange={(e) => update('category', e.target.value as BoardCategory)}
                >
                  {form.categoryOptions.map((opt) => (
                    <MenuItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  size="small"
                  label="제목"
                  required
                  value={values.title}
                  onChange={(e) => update('title', e.target.value)}
                  onBlur={validation.onBlur('title')}
                  error={validation.isInvalid('title')}
                  helperText={validation.errorMessage('title')}
                />
              </CategoryTitleGrid>
              <TextField
                size="small"
                label="내용"
                required
                multiline
                minRows={12}
                value={values.content}
                onChange={(e) => update('content', e.target.value)}
                onBlur={validation.onBlur('content')}
                error={validation.isInvalid('content')}
                helperText={validation.errorMessage('content')}
              />
              <FileAttachField
                value={values.attachments}
                onChange={form.updateAttachments}
                disabled={form.isSaving}
              />
            </FieldColumn>
          </FormSection>
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title={mode === 'create' ? '게시글 등록' : '게시글 수정'}
        message={
          mode === 'create'
            ? `${values.title.trim() || '작성한 게시글'} 을(를) 등록하시겠습니까?`
            : `${values.title.trim() || '작성한 게시글'} 을(를) 저장하시겠습니까?`
        }
        confirmLabel={
          form.isSaving
            ? mode === 'create' ? '등록 중...' : '저장 중...'
            : mode === 'create' ? '등록' : '저장'
        }
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
