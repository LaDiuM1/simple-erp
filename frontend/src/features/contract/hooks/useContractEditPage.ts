import { useGetContractQuery } from '@/features/contract/api/contractApi';

/**
 * 계약 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 ContractEditForm Body 안에서 렌더.
 */
export function useContractEditPage(id: number) {
  const detailQuery = useGetContractQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
