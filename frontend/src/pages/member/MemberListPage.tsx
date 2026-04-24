import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { PrimaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import {
  MEMBER_MENU_CODE,
  memberColumn,
  memberSearchFilter,
  useMemberListApi,
} from '@/features/member/config/memberListConfig';

export default function MemberListPage() {
  const navigate = useNavigate();
  const api = useMemberListApi();

  return (
    <>
      <PageHeaderActions>
        <PermissionGate menuCode={MEMBER_MENU_CODE} action="write">
          <PrimaryPageHeaderButton
            startIcon={<AddIcon />}
            onClick={() => navigate('/members/new')}
          >
            직원 등록
          </PrimaryPageHeaderButton>
        </PermissionGate>
      </PageHeaderActions>

      <GenericList api={api} searchFilter={memberSearchFilter} column={memberColumn} />
    </>
  );
}
