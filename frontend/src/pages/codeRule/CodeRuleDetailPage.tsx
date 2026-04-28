import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import Muted from '@/shared/ui/atoms/Muted';
import GenericHeaderDetails, {
  type HeaderDetailField,
} from '@/shared/ui/GenericHeaderDetails';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { usePermission } from '@/shared/hooks/usePermission';
import { useGetCodeRuleQuery } from '@/features/codeRule/api/codeRuleApi';
import {
  CODE_RULE_TARGET,
  CODE_RULE_TARGET_LABEL,
  INPUT_MODE_LABEL,
  RESET_POLICY_LABEL,
  type CodeRule,
  type CodeRuleTarget,
} from '@/features/codeRule/types';
import { getErrorMessage } from '@/shared/api/error';
import { DetailRoot, Mono } from './CodeRuleDetailPage.styles';

const VALID_TARGETS = new Set<string>(Object.values(CODE_RULE_TARGET));

/**
 * 코드 채번 규칙 상세 — 순수 정보 표기. 헤더 우측에 [목록으로] + (canWrite) [수정].
 * 채번 규칙은 enum 기반으로 항목 수가 고정 — Create/Delete 가 없으므로 읽기 + 수정 진입만 제공.
 * 본문은 다른 도메인 상세와 동일하게 GenericHeaderDetails 매트릭스로 표기.
 */
export default function CodeRuleDetailPage() {
  const { target } = useParams<{ target: string }>();
  if (!target || !VALID_TARGETS.has(target)) return null;
  return <Body target={target as CodeRuleTarget} />;
}

function Body({ target }: { target: CodeRuleTarget }) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.CODE_RULES);
  const { data, isLoading, isError, error, refetch } = useGetCodeRuleQuery(target);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  if (!data) return null;

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '목록으로',
            icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(MENU_PATH[MENU_CODE.CODE_RULES]),
          },
          ...(canWrite
            ? [
                {
                  design: 'create' as const,
                  label: '수정',
                  icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
                  onClick: () => navigate(`${MENU_PATH[MENU_CODE.CODE_RULES]}/${target}/edit`),
                  menuCode: MENU_CODE.CODE_RULES,
                },
              ]
            : []),
        ]}
      />

      <DetailRoot>
        <GenericHeaderDetails fields={ruleInfoFields(data)} />
      </DetailRoot>
    </>
  );
}

function ruleInfoFields(rule: CodeRule): HeaderDetailField[] {
  return [
    { label: '대상', value: CODE_RULE_TARGET_LABEL[rule.target] },
    { label: '접두사', value: rule.prefix ? <Mono>{rule.prefix}</Mono> : null },
    { label: '패턴', value: <Mono>{rule.pattern}</Mono> },
    { label: '기본 순번 자릿수', value: `${rule.defaultSeqLength} 자리` },
    { label: '입력 방식', value: INPUT_MODE_LABEL[rule.inputMode] },
    { label: '초기화 주기', value: RESET_POLICY_LABEL[rule.resetPolicy] },
    { label: '부모별 시퀀스', value: rule.parentScoped ? '예' : '아니오' },
    {
      label: '다음 코드',
      value: rule.nextCode ? (
        <Mono>{rule.nextCode}</Mono>
      ) : (
        <Muted>{'{PARENT} 토큰 사용 — 부모별 미리보기는 수정 화면에서 확인'}</Muted>
      ),
    },
    { label: '메모', value: rule.description, fullWidth: true },
  ];
}
