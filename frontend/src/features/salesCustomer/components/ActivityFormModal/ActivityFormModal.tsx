import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import EmployeeSelectField from '@/features/employee/components/EmployeeSelectField';
import SalesContactSelectField from '@/features/salesContact/components/SalesContactSelectField';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import {
  useCreateSalesActivityMutation,
  useUpdateSalesActivityMutation,
} from '@/features/salesCustomer/api/salesCustomerApi';
import {
  SALES_ACTIVITY_TYPE_OPTIONS,
  isoToDateTimeLocal,
  todayDateTimeLocal,
  type SalesActivity,
  type SalesActivityType,
} from '@/features/salesCustomer/types';
import { getErrorMessage } from '@/shared/api/error';

interface FormValues {
  type: SalesActivityType;
  activityDate: string;
  subject: string;
  content: string;
  ourEmployeeId: string;
  ourEmployeeName: string;
  customerContactId: string;
  customerContactSelectedName: string;
}

const EMPTY: FormValues = {
  type: 'VISIT',
  activityDate: '',
  subject: '',
  content: '',
  ourEmployeeId: '',
  ourEmployeeName: '',
  customerContactId: '',
  customerContactSelectedName: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  customerId: number;
  /** 수정 모드 — 기존 활동 데이터. 미지정 시 등록 모드. */
  activity?: SalesActivity;
}

export default function ActivityFormModal({ open, onClose, customerId, activity }: Props) {
  const isEdit = activity !== undefined;
  const snackbar = useSnackbar();
  const [createMut, { isLoading: isCreating }] = useCreateSalesActivityMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateSalesActivityMutation();
  const { data: myProfile } = useGetMyProfileQuery();

  const [values, setValues] = useState<FormValues>(() =>
    activity ? toFormValues(activity) : { ...EMPTY, activityDate: todayDateTimeLocal() },
  );

  React.useEffect(() => {
    if (!open) return;
    if (activity) {
      setValues(toFormValues(activity));
    } else {
      setValues({
        ...EMPTY,
        activityDate: todayDateTimeLocal(),
        ourEmployeeId: myProfile ? String(myProfile.id) : '',
        ourEmployeeName: myProfile ? myProfile.name : '',
      });
    }
  }, [open, activity, myProfile]);

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.subject.trim() === '') {
      snackbar.error('제목을 입력해주세요.');
      return;
    }
    if (values.activityDate === '') {
      snackbar.error('활동 일시를 선택해주세요.');
      return;
    }
    if (values.ourEmployeeId === '') {
      snackbar.error('우리 담당자를 선택해주세요.');
      return;
    }

    const payload = {
      type: values.type,
      activityDate: ensureSeconds(values.activityDate),
      subject: values.subject.trim(),
      content: emptyToNull(values.content),
      ourEmployeeId: Number(values.ourEmployeeId),
      customerContactId: values.customerContactId !== '' ? Number(values.customerContactId) : null,
    };

    try {
      if (isEdit && activity) {
        await updateMut({ id: activity.id, customerId, body: payload }).unwrap();
        snackbar.success('활동이 수정되었습니다.');
      } else {
        await createMut({ customerId, ...payload }).unwrap();
        snackbar.success('활동이 등록되었습니다.');
      }
      onClose();
    } catch (err) {
      snackbar.error(getErrorMessage(err, '저장 중 오류가 발생했습니다.'));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '영업 활동 수정' : '영업 활동 등록'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                select
                size="small"
                label="활동 유형"
                required
                value={values.type}
                onChange={(e) => update('type', e.target.value as SalesActivityType)}
                sx={{ flex: 1 }}
              >
                {SALES_ACTIVITY_TYPE_OPTIONS.map((opt) => (
                  <MenuItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                size="small"
                type="datetime-local"
                label="활동 일시"
                required
                value={values.activityDate}
                onChange={(e) => update('activityDate', e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
                sx={{ flex: 1 }}
              />
            </Stack>
            <TextField
              size="small"
              label="제목"
              required
              value={values.subject}
              onChange={(e) => update('subject', e.target.value)}
              slotProps={{ htmlInput: { maxLength: 200 } }}
            />
            <EmployeeSelectField
              label="우리 담당자"
              required
              value={values.ourEmployeeId}
              valueLabel={values.ourEmployeeName}
              onChange={(id, name) => {
                update('ourEmployeeId', id);
                update('ourEmployeeName', name);
              }}
            />

            <SalesContactSelectField
              label="고객사 담당자 (영업 명부)"
              value={values.customerContactId}
              valueLabel={values.customerContactSelectedName}
              onChange={(id, name) => {
                update('customerContactId', id);
                update('customerContactSelectedName', name);
              }}
            />

            <TextField
              size="small"
              multiline
              minRows={4}
              label="내용"
              value={values.content}
              onChange={(e) => update('content', e.target.value)}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>
            취소
          </Button>
          <Button type="submit" variant="contained" disabled={isSaving}>
            {isSaving ? '저장 중...' : isEdit ? '저장' : '등록'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function toFormValues(a: SalesActivity): FormValues {
  return {
    type: a.type,
    activityDate: isoToDateTimeLocal(a.activityDate),
    subject: a.subject,
    content: a.content ?? '',
    ourEmployeeId: String(a.ourEmployeeId),
    ourEmployeeName: a.ourEmployeeName ?? '',
    customerContactId: a.customerContactId == null ? '' : String(a.customerContactId),
    customerContactSelectedName: a.customerContactRegisteredName ?? '',
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}

/** datetime-local 입력은 초 단위가 없어 ":00" 추가 — BE LocalDateTime 파싱 안전성. */
function ensureSeconds(dt: string): string {
  return dt.length === 16 ? `${dt}:00` : dt;
}
