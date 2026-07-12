import { useGetAfterServiceQuery } from '@/features/afterService/api/afterServiceApi';

/**
 * AS 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 AfterServiceEditForm Body 안에서 렌더.
 */
export function useAfterServiceEditPage(id: number) {
  const detailQuery = useGetAfterServiceQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
