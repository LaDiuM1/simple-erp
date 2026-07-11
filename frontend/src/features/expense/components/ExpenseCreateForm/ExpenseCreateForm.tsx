import IconButton from '@mui/material/IconButton';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import Tooltip from '@mui/material/Tooltip';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import HowToRegRoundedIcon from '@mui/icons-material/HowToRegRounded';
import ReceiptLongRoundedIcon from '@mui/icons-material/ReceiptLongRounded';
import { FormSection } from '@/shared/ui/GenericForm';
import ApprovalLineField from '@/shared/ui/ApprovalLineField';
import FileAttachField from '@/shared/ui/FileAttachField';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import type { ExpenseCreateFormState } from '@/features/expense/hooks/useExpenseCreateForm';
import { formatKrw } from '@/features/expense/utils/formatKrw';
import {
  EXPENSE_CATEGORY_OPTIONS,
  type ExpenseCategory,
} from '@/features/expense/types';
import {
  AddItemButton,
  CreateForm,
  CreateRoot,
  FieldGrid,
  ItemCard,
  ItemCardHeader,
  ItemCardTitle,
  ItemFieldGrid,
  ItemStack,
  TotalLabel,
  TotalRow,
  TotalValue,
} from './ExpenseCreateForm.styles';

export const EXPENSE_CREATE_FORM_ID = 'expense-create-form';

interface Props {
  form: ExpenseCreateFormState;
}

/**
 * 경비 등록 Body — form-state hook 결과를 JSX 로 렌더 (fetch 미관여).
 * 저장/취소 버튼은 page hook 의 headerActions 가 formId 로 연결.
 */
export default function ExpenseCreateForm({ form }: Props) {
  return (
    <>
      <CreateRoot>
        <CreateForm id={EXPENSE_CREATE_FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <FormSection
            icon={<ReceiptLongRoundedIcon sx={{ fontSize: 18 }} />}
            title="기본 정보"
            description="경비 청구 제목 — 결재 문서 제목으로 사용."
          >
            <FieldGrid>
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
            </FieldGrid>
          </FormSection>

          <FormSection
            icon={<ReceiptLongRoundedIcon sx={{ fontSize: 18 }} />}
            title="경비 항목"
            description="지출 건별 일자 / 분류 / 금액 / 내용 / 영수증."
          >
            <ItemStack>
              {form.values.items.map((item, index) => (
                <ItemCard key={item.rowId}>
                  <ItemCardHeader>
                    <ItemCardTitle>항목 {index + 1}</ItemCardTitle>
                    <Tooltip title="항목 제거" arrow>
                      <IconButton
                        size="small"
                        onClick={() => form.removeItem(item.rowId)}
                        sx={{ '&:hover': { color: 'error.main' } }}
                        aria-label="항목 제거"
                      >
                        <DeleteOutlineIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </ItemCardHeader>
                  <ItemFieldGrid>
                    <TextField
                      size="small"
                      type="date"
                      label="지출 일자"
                      required
                      value={item.expenseDate}
                      onChange={(e) => form.updateItem(item.rowId, 'expenseDate', e.target.value)}
                      slotProps={{ inputLabel: { shrink: true } }}
                    />
                    <TextField
                      select
                      size="small"
                      label="분류"
                      required
                      value={item.category}
                      onChange={(e) =>
                        form.updateItem(item.rowId, 'category', e.target.value as ExpenseCategory)
                      }
                    >
                      {EXPENSE_CATEGORY_OPTIONS.map((opt) => (
                        <MenuItem key={opt.value} value={opt.value}>
                          {opt.label}
                        </MenuItem>
                      ))}
                    </TextField>
                    <TextField
                      size="small"
                      type="number"
                      label="금액"
                      required
                      value={item.amount}
                      onChange={(e) => form.updateItem(item.rowId, 'amount', e.target.value)}
                      placeholder="0"
                      slotProps={{ htmlInput: { min: 0 } }}
                    />
                    <TextField
                      size="small"
                      label="내용"
                      value={item.description}
                      onChange={(e) => form.updateItem(item.rowId, 'description', e.target.value)}
                      placeholder="지출 내용"
                      slotProps={{ htmlInput: { maxLength: 500 } }}
                    />
                  </ItemFieldGrid>
                  <FileAttachField
                    label="영수증"
                    single
                    value={item.receipt}
                    onChange={(apply) => form.updateItemReceipt(item.rowId, apply)}
                    disabled={form.isSaving}
                  />
                </ItemCard>
              ))}
              <AddItemButton variant="outlined" size="small" startIcon={<AddRoundedIcon />} onClick={form.addItem}>
                항목 추가
              </AddItemButton>
              <TotalRow>
                <TotalLabel>합계</TotalLabel>
                <TotalValue>{formatKrw(form.totalAmount)}</TotalValue>
              </TotalRow>
            </ItemStack>
          </FormSection>

          <FormSection
            icon={<HowToRegRoundedIcon sx={{ fontSize: 18 }} />}
            title="결재선"
            description="추가 순서 = 결재 순서. 등록 즉시 1차 결재자에게 상신."
          >
            <ApprovalLineField
              label="결재자"
              value={form.values.approvalLine}
              onChange={(line) => form.update('approvalLine', line)}
              disabled={form.isSaving}
            />
          </FormSection>
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="경비 청구 상신"
        message={`${form.values.title.trim() || '작성한 경비 청구'} 을(를) 상신하시겠습니까? 등록 즉시 결재가 시작됩니다.`}
        confirmLabel={form.isSaving ? '상신 중...' : '상신'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
