/** 원천 데이터 변경이 영향을 주는 파생 조회 태그를 한곳에서 관리한다. */
export const DASHBOARD_CACHE_TAGS = {
  summary: { type: 'Dashboard', id: 'SUMMARY' },
  sales: { type: 'Dashboard', id: 'SALES' },
  service: { type: 'Dashboard', id: 'SERVICE' },
  warranty: { type: 'Dashboard', id: 'WARRANTY' },
} as const;

export const PROFILE_CACHE_TAG = { type: 'Employee', id: 'MY_PROFILE' } as const;

/**
 * 원천 이름은 목록뿐 아니라 여러 상세 응답에도 복제된다. 변경된 원천이 어느 상세 ID에
 * 포함됐는지는 클라이언트가 알 수 없으므로, 영향을 받는 집계 엔티티 타입만 선별해 갱신한다.
 */
export const DERIVED_CACHE_TAGS = {
  customer: [
    { type: 'Contract' },
    { type: 'Equipment' },
    { type: 'AfterService' },
    { type: 'SalesContact' },
    { type: 'SalesContactEmployment' },
    DASHBOARD_CACHE_TAGS.summary,
  ],
  supplier: [
    { type: 'Product' },
    { type: 'Contract' },
    { type: 'Equipment' },
  ],
  product: [
    { type: 'Product', id: 'REFERENCE_LIST' },
    { type: 'Contract' },
    { type: 'Equipment' },
    { type: 'AfterService' },
  ],
  employee: [
    { type: 'Employee', id: 'REFERENCE_LIST' },
    { type: 'Employee', id: 'CONTRACT_REFERENCE_LIST' },
    { type: 'Contract' },
    { type: 'AfterService' },
    { type: 'Engineer', id: 'LIST' },
    { type: 'SalesActivity' },
    { type: 'SalesAssignment' },
    DASHBOARD_CACHE_TAGS.summary,
    DASHBOARD_CACHE_TAGS.service,
    PROFILE_CACHE_TAG,
  ],
  organization: [
    { type: 'Employee' },
    { type: 'SalesActivity' },
    { type: 'SalesAssignment' },
  ],
  role: [
    { type: 'Employee' },
  ],
  equipment: [{ type: 'AfterService' }],
  engineer: [{ type: 'AfterService' }],
} as const;
