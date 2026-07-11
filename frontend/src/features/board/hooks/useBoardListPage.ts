import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetPostsQuery } from '@/features/board/api/boardApi';
import type { PostListFilters, PostSummary } from '@/features/board/types';

/**
 * 게시판 목록 page hook — api + headerActions 묶음.
 * 게시판은 전 직원 작성 가능이라 [글 작성] 은 canWrite 가 아닌 canRead 로 노출
 * (NOTICE 카테고리 제한은 폼의 카테고리 옵션에서 처리).
 */
export function useBoardListPage(): {
  api: ListApiConfig<PostSummary, PostListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();
  const { canRead } = usePermission(MENU_CODE.BOARDS);

  const api: ListApiConfig<PostSummary, PostListFilters> = {
    menuCode: MENU_CODE.BOARDS,
    useList: useGetPostsQuery,
    rowKey: (m) => m.id,
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.BOARDS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = canRead
    ? [
        {
          design: 'create',
          label: '글 작성',
          onClick: () => navigate(`${MENU_PATH[MENU_CODE.BOARDS]}/new`),
        },
      ]
    : [];

  return { api, headerActions };
}
