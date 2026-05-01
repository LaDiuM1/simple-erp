import { useGetCustomerQuery } from '@/features/customer/api/customerApi';

/**
 * 고객사 상세 page hook — outer fetch 만 노출.
 * headerActions 는 CustomerDetailForm 가 자체 렌더 (read-only 폼 톤 유지).
 */
export function useCustomerDetailPage(id: number) {
  const detailQuery = useGetCustomerQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
