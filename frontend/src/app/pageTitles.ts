/**
 * 경로 → 페이지 제목 매핑. AppLayout 의 PageHeader 가 useLocation().pathname 으로 조회.
 * 정적 매핑이 먼저, 매치 안되면 동적 패턴 매처가 시도.
 */

const STATIC_TITLES: Record<string, string> = {
  '/': '대시보드',
  '/member/me': '내 정보',
  '/members': '직원 목록',
  '/members/new': '직원 등록',
};

interface DynamicRule {
  test: (pathname: string) => boolean;
  title: string;
}

const DYNAMIC_RULES: DynamicRule[] = [
  { test: (p) => /^\/members\/\d+\/edit$/.test(p), title: '직원 수정' },
];

export function getPageTitle(pathname: string): string {
  if (STATIC_TITLES[pathname]) return STATIC_TITLES[pathname];
  for (const rule of DYNAMIC_RULES) {
    if (rule.test(pathname)) return rule.title;
  }
  return '';
}
