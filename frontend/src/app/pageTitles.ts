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
};

interface DynamicRule {
  test: (pathname: string) => boolean;
  title: string;
}

const DYNAMIC_RULES: DynamicRule[] = [
  { test: (p) => /^\/employees\/\d+\/edit$/.test(p), title: '직원 수정' },
  { test: (p) => /^\/departments\/\d+\/edit$/.test(p), title: '부서 수정' },
  { test: (p) => /^\/positions\/\d+\/edit$/.test(p), title: '직책 수정' },
  { test: (p) => /^\/roles\/\d+\/edit$/.test(p), title: '권한 수정' },
  { test: (p) => /^\/code-rules\/[^/]+\/edit$/.test(p), title: '코드 채번 규칙 수정' },
];

export function getPageTitle(pathname: string): string {
  if (STATIC_TITLES[pathname]) return STATIC_TITLES[pathname];
  for (const rule of DYNAMIC_RULES) {
    if (rule.test(pathname)) return rule.title;
  }
  return '';
}
