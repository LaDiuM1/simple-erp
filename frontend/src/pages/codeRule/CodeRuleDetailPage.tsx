import { useParams } from 'react-router-dom';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import PageHeaderTitle from '@/shared/ui/layout/PageHeaderTitle';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import {
  ruleInfoFields,
  useCodeRuleDetailPage,
} from '@/features/codeRule/hooks/useCodeRuleDetailPage';
import {
  CODE_RULE_TARGET,
  type CodeRuleTarget,
} from '@/features/codeRule/types';
import { DetailRoot } from './CodeRuleDetailPage.styles';

const VALID_TARGETS = new Set<string>(Object.values(CODE_RULE_TARGET));

export default function CodeRuleDetailPage() {
  const { target } = useParams<{ target: string }>();
  if (!target || !VALID_TARGETS.has(target)) return null;

  const { queries, headerActions, pageTitle } = useCodeRuleDetailPage(target as CodeRuleTarget);

  return (
    <>
      <PageHeaderTitle>{pageTitle}</PageHeaderTitle>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={ruleInfoFields(detail)} />
          </DetailRoot>
        )}
      </QueryGate>
    </>
  );
}
