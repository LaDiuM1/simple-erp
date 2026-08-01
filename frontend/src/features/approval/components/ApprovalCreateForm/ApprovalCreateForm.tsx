import TextField from '@mui/material/TextField';
import HistoryEduRoundedIcon from '@mui/icons-material/HistoryEduRounded';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { FormSection } from '@/shared/ui/GenericForm';
import ApprovalLineField from '@/shared/ui/ApprovalLineField';
import FileAttachField from '@/shared/ui/FileAttachField';
import ContentTextField from '@/shared/ui/ContentTextField';
import { useApprovalCreateForm } from '@/features/approval/hooks/useApprovalCreateForm';
import { CreateForm, CreateRoot, FieldStack } from './ApprovalCreateForm.styles';

const FORM_ID = 'approval-create-form';

export default function ApprovalCreateForm() {
  const form = useApprovalCreateForm();

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'create',
            label: '상신',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.APPROVALS,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <FormSection
            icon={<HistoryEduRoundedIcon sx={{ fontSize: 18 }} />}
            title="기안 내용"
            description="결재를 요청할 문서의 제목과 본문을 작성하세요."
          >
            <FieldStack>
              <TextField
                size="small"
                label="제목"
                required
                value={form.values.title}
                onChange={(e) => form.update('title', e.target.value)}
                onBlur={form.validation.onBlur('title')}
                error={form.validation.isInvalid('title')}
                helperText={form.validation.errorMessage('title')}
              />
              <ContentTextField
                label="본문"
                minRows={8}
                value={form.values.content}
                onChange={(value) => form.update('content', value)}
                onBlur={form.validation.onBlur('content')}
                error={form.validation.isInvalid('content')}
                helperText={form.validation.errorMessage('content')}
              />
            </FieldStack>
          </FormSection>
          <FormSection>
            <FieldStack>
              <ApprovalLineField
                value={form.values.line}
                onChange={(line) => form.update('line', line)}
                disabled={form.isSaving}
              />
              <FileAttachField
                value={form.values.attachments}
                onChange={form.updateAttachments}
                disabled={form.isSaving}
              />
            </FieldStack>
          </FormSection>
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="기안 상신"
        message={`${form.values.title.trim() || '작성한 기안'} 을(를) 상신하시겠습니까?`}
        confirmLabel={form.isSaving ? '상신 중...' : '상신'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
