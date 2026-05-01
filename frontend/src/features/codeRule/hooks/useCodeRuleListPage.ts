import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import { useGetCodeRulesQuery } from '@/features/codeRule/api/codeRuleApi';
import type { CodeRule } from '@/features/codeRule/types';

/**
 * 코드 채번 규칙 목록 page hook — query + 행 클릭 핸들러 (권한별 진입 경로 분기).
 * GenericList 가 부적합 (페이징·검색·삭제 부재) — styled primitives 만 재사용.
 */
export function useCodeRuleListPage() {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.CODE_RULES);
  const listQuery = useGetCodeRulesQuery();

  const onRowClick = (rule: CodeRule) =>
    navigate(canWrite ? `/code-rules/${rule.target}/edit` : `/code-rules/${rule.target}`);

  return {
    queries: { rules: listQuery },
    onRowClick,
  };
}
