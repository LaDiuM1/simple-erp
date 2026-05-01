import { useGetRoleQuery } from '@/features/role/api/roleApi';

/**
 * 권한 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 RoleForm 안에서 렌더.
 */
export function useRoleEditPage(id: number) {
  const detailQuery = useGetRoleQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
