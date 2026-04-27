import { useNavigate } from 'react-router-dom';
import LowPriorityRoundedIcon from '@mui/icons-material/LowPriorityRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  positionColumn,
  positionSearchFilter,
  usePositionListApi,
} from '@/features/position/config/positionListConfig';

export default function PositionListPage() {
  const navigate = useNavigate();
  const api = usePositionListApi();

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '직책 서열 관리',
            icon: <LowPriorityRoundedIcon />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/ranking`),
          },
          {
            design: 'create',
            label: '직책 등록',
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/new`),
            menuCode: MENU_CODE.POSITIONS,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={positionSearchFilter}
        column={positionColumn}
      />
    </>
  );
}
