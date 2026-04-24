import type { FilterOption } from './types';

/**
 * SelectFilterItem.mapOptions 에 그대로 꽂아 쓰는 공용 매퍼.
 * 서버가 돌려주는 `{ id: number; name: string }[]` 배열을 FilterOption[] 으로 변환한다.
 * 부서/직책/권한 등 참조 테이블 드롭다운에서 공통으로 사용.
 */
export const mapIdName = (data: unknown): FilterOption[] =>
  (data as { id: number; name: string }[]).map((x) => ({ value: x.id, label: x.name }));
