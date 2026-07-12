import { useGetEquipmentQuery } from '@/features/equipment/api/equipmentApi';

/**
 * 설비 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 EquipmentEditForm Body 안에서 렌더.
 */
export function useEquipmentEditPage(id: number) {
  const detailQuery = useGetEquipmentQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
