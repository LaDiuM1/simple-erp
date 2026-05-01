import { useGetCustomerQuery } from '@/features/customer/api/customerApi';

/**
 * 고객사 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 CustomerEditForm Body 안에서 렌더.
 */
export function useCustomerEditPage(id: number) {
  const detailQuery = useGetCustomerQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
