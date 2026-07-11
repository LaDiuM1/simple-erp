import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import LeaveCreateForm from '@/features/attendance/components/LeaveCreateForm/LeaveCreateForm';
import { useLeaveCreatePage } from '@/features/attendance/hooks/useLeaveCreatePage';

/** 휴가 신청 — 커스텀 폼 (page hook + Body 컴포넌트). */
export default function LeaveCreatePage() {
  const { form, headerActions } = useLeaveCreatePage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <LeaveCreateForm form={form} />
    </>
  );
}
