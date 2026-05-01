import QueryGate from '@/shared/ui/feedback/QueryGate';
import EmployeeProfileCard from '@/features/employee/components/EmployeeProfileCard';
import { useEmployeeMePage } from '@/features/employee/hooks/useEmployeeMePage';
import { PageRoot, PageTitle } from './EmployeeMePage.styles';

export default function EmployeeMePage() {
  const { queries } = useEmployeeMePage();

  return (
    <QueryGate queries={queries}>
      {({ profile }) => (
        <PageRoot>
          <PageTitle>내 정보</PageTitle>
          <EmployeeProfileCard profile={profile} />
        </PageRoot>
      )}
    </QueryGate>
  );
}
