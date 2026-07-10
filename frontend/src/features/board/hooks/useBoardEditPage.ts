import { useGetPostQuery } from '@/features/board/api/boardApi';

/**
 * 게시글 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 BoardForm Body 안에서 렌더.
 */
export function useBoardEditPage(id: number) {
  const detailQuery = useGetPostQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
