import { useParams } from 'react-router-dom';
import CodeRuleEditForm from '@/features/codeRule/components/CodeRuleEditForm/CodeRuleEditForm';
import { CODE_RULE_TARGET, type CodeRuleTarget } from '@/features/codeRule/types';

const VALID_TARGETS = new Set<string>(Object.values(CODE_RULE_TARGET));

export default function CodeRuleEditPage() {
  const { target } = useParams<{ target: string }>();
  if (!target || !VALID_TARGETS.has(target)) return null;
  return <CodeRuleEditForm target={target as CodeRuleTarget} />;
}
