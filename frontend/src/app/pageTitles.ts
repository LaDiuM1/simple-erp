/**
 * 경로 → 페이지 제목 매핑. AppLayout 의 PageHeader 가 useLocation().pathname 으로 조회.
 * 정적 매핑이 먼저, 매치 안되면 동적 패턴 매처가 시도.
 */

const STATIC_TITLES: Record<string, string> = {
  '/': '대시보드',
  '/employee/me': '내 정보',
  '/employees': '직원 목록',
  '/employees/new': '직원 등록',
  '/departments': '부서 목록',
  '/departments/new': '부서 등록',
  '/departments/hierarchy': '부서 계층 관리',
  '/positions': '직책 목록',
  '/positions/new': '직책 등록',
  '/positions/ranking': '직책 서열 관리',
  '/roles': '권한 목록',
  '/roles/new': '권한 등록',
  '/code-rules': '코드 채번 규칙',
  '/customers': '고객사 목록',
  '/customers/new': '고객사 등록',
  '/suppliers': '공급사 목록',
  '/suppliers/new': '공급사 등록',
  '/products': '제품 모델 목록',
  '/products/new': '제품 모델 등록',
  '/products/categories': '제품 카테고리 관리',
  '/sales-customers': '고객사 영업 관리',
  '/sales-contacts': '영업 명부 목록',
  '/sales-contacts/new': '영업 명부 등록',
  '/contracts': '계약 목록',
  '/contracts/new': '계약 등록',
  '/equipments': '설비 대장',
  '/equipments/new': '설비 등록',
  '/after-services': 'AS 목록',
  '/after-services/new': 'AS 접수',
  '/after-services/engineers': '엔지니어 관리',
  '/approvals': '전자결재',
  '/approvals/new': '기안 작성',
  '/expenses': '경비 목록',
  '/expenses/new': '경비 등록',
  '/attendance': '내 출퇴근',
  '/attendance/status': '근태 현황',
  '/leaves': '휴가 관리',
  '/leaves/new': '휴가 신청',
  '/leaves/status': '휴가 현황',
  '/leaves/balances': '연차 잔여 관리',
  '/boards': '게시판',
  '/boards/new': '글 작성',
  '/drive': '드라이브',
};

interface DynamicRule {
  test: (pathname: string) => boolean;
  title: string;
}

const DYNAMIC_RULES: DynamicRule[] = [
  { test: (p) => /^\/employees\/\d+\/edit$/.test(p), title: '직원 수정' },
  { test: (p) => /^\/employees\/\d+$/.test(p), title: '직원 상세' },
  { test: (p) => /^\/departments\/\d+\/edit$/.test(p), title: '부서 수정' },
  { test: (p) => /^\/departments\/\d+$/.test(p), title: '부서 상세' },
  { test: (p) => /^\/positions\/\d+\/edit$/.test(p), title: '직책 수정' },
  { test: (p) => /^\/positions\/\d+$/.test(p), title: '직책 상세' },
  { test: (p) => /^\/roles\/\d+\/edit$/.test(p), title: '권한 수정' },
  { test: (p) => /^\/roles\/\d+$/.test(p), title: '권한 상세' },
  { test: (p) => /^\/code-rules\/[^/]+\/edit$/.test(p), title: '코드 채번 규칙 수정' },
  { test: (p) => /^\/code-rules\/[^/]+$/.test(p), title: '코드 채번 규칙 상세' },
  { test: (p) => /^\/customers\/\d+\/edit$/.test(p), title: '고객사 수정' },
  { test: (p) => /^\/customers\/\d+$/.test(p), title: '고객사 상세' },
  { test: (p) => /^\/suppliers\/\d+\/edit$/.test(p), title: '공급사 수정' },
  { test: (p) => /^\/suppliers\/\d+$/.test(p), title: '공급사 상세' },
  { test: (p) => /^\/products\/\d+\/edit$/.test(p), title: '제품 모델 수정' },
  { test: (p) => /^\/products\/\d+$/.test(p), title: '제품 모델 상세' },
  { test: (p) => /^\/sales-customers\/\d+$/.test(p), title: '고객사 영업 상세' },
  { test: (p) => /^\/sales-contacts\/\d+\/edit$/.test(p), title: '영업 명부 수정' },
  { test: (p) => /^\/sales-contacts\/\d+$/.test(p), title: '영업 명부 상세' },
  { test: (p) => /^\/contracts\/\d+\/edit$/.test(p), title: '계약 수정' },
  { test: (p) => /^\/contracts\/\d+$/.test(p), title: '계약 상세' },
  { test: (p) => /^\/equipments\/\d+\/edit$/.test(p), title: '설비 수정' },
  { test: (p) => /^\/equipments\/\d+$/.test(p), title: '설비 상세' },
  { test: (p) => /^\/after-services\/\d+\/edit$/.test(p), title: 'AS 수정' },
  { test: (p) => /^\/after-services\/\d+$/.test(p), title: 'AS 상세' },
  { test: (p) => /^\/approvals\/\d+$/.test(p), title: '결재 문서' },
  { test: (p) => /^\/expenses\/\d+$/.test(p), title: '경비 상세' },
  { test: (p) => /^\/boards\/\d+\/edit$/.test(p), title: '게시글 수정' },
  { test: (p) => /^\/boards\/\d+$/.test(p), title: '게시글 상세' },
];

export function getPageTitle(pathname: string): string {
  if (STATIC_TITLES[pathname]) return STATIC_TITLES[pathname];
  for (const rule of DYNAMIC_RULES) {
    if (rule.test(pathname)) return rule.title;
  }
  return '';
}
