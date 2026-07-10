import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  approvalListColumns,
  approvalListFilters,
} from '@/features/approval/config/approvalListConfig';
import { useApprovalListPage } from '@/features/approval/hooks/useApprovalListPage';

export default function ApprovalListPage() {
  const { api, headerActions } = useApprovalListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={approvalListFilters} column={approvalListColumns} />
    </>
  );
}
