import { useGetCodeRuleQuery } from '@/features/codeRule/api/codeRuleApi';
import type { CodeRuleTarget } from '@/features/codeRule/types';

/**
 * 코드 채번 규칙 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 CodeRuleEditForm Body 안에서 렌더.
 */
export function useCodeRuleEditPage(target: CodeRuleTarget) {
  const detailQuery = useGetCodeRuleQuery(target);
  return { queries: { detail: detailQuery } };
}
