import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import CodeRuleEditForm from '@/features/codeRule/components/CodeRuleEditForm/CodeRuleEditForm';
import { useCodeRuleEditPage } from '@/features/codeRule/hooks/useCodeRuleEditPage';
import { CODE_RULE_TARGET, type CodeRuleTarget } from '@/features/codeRule/types';

const VALID_TARGETS = new Set<string>(Object.values(CODE_RULE_TARGET));

export default function CodeRuleEditPage() {
  const { target } = useParams<{ target: string }>();
  if (!target || !VALID_TARGETS.has(target)) return null;

  const codeRuleTarget = target as CodeRuleTarget;
  return <CodeRuleEditContent target={codeRuleTarget} />;
}

function CodeRuleEditContent({ target }: { target: CodeRuleTarget }) {
  const { queries } = useCodeRuleEditPage(target);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <CodeRuleEditForm target={target} rule={detail} />}
    </QueryGate>
  );
}
