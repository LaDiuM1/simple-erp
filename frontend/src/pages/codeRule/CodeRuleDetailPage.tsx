import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import Muted from '@/shared/ui/atoms/Muted';
import { FormSection } from '@/shared/ui/GenericForm';
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
import {
  ContentBox,
  InfoGrid,
  InfoLabel,
  InfoValue,
  Mono,
  PageRoot,
  PageSurface,
} from './CodeRuleDetailPage.styles';

const VALID_TARGETS = new Set<string>(Object.values(CODE_RULE_TARGET));

/**
 * 코드 채번 규칙 상세 — 순수 정보 표기. 헤더 우측에 [목록으로] + (canWrite) [수정].
 * 채번 규칙은 enum 기반으로 항목 수가 고정 — Create/Delete 가 없으므로 읽기 + 수정 진입만 제공.
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

      <PageRoot>
        <PageSurface>
          <ContentBox>
            <DetailContent rule={data} />
          </ContentBox>
        </PageSurface>
      </PageRoot>
    </>
  );
}

function DetailContent({ rule }: { rule: CodeRule }) {
  return (
    <>
      <FormSection title="규칙 정보" description="현재 적용 중인 채번 규칙의 설정값입니다.">
        <InfoGrid>
          <InfoLabel>대상</InfoLabel>
          <InfoValue>{CODE_RULE_TARGET_LABEL[rule.target]}</InfoValue>

          <InfoLabel>접두사</InfoLabel>
          <InfoValue>{rule.prefix ? <Mono>{rule.prefix}</Mono> : <Muted />}</InfoValue>

          <InfoLabel>패턴</InfoLabel>
          <InfoValue><Mono>{rule.pattern}</Mono></InfoValue>

          <InfoLabel>기본 순번 자릿수</InfoLabel>
          <InfoValue>{rule.defaultSeqLength} 자리</InfoValue>

          <InfoLabel>입력 방식</InfoLabel>
          <InfoValue>{INPUT_MODE_LABEL[rule.inputMode]}</InfoValue>

          <InfoLabel>초기화 주기</InfoLabel>
          <InfoValue>{RESET_POLICY_LABEL[rule.resetPolicy]}</InfoValue>

          <InfoLabel>부모별 시퀀스</InfoLabel>
          <InfoValue>{rule.parentScoped ? '예' : '아니오'}</InfoValue>

          <InfoLabel>다음 코드</InfoLabel>
          <InfoValue>
            {rule.nextCode ? <Mono>{rule.nextCode}</Mono> : <Muted>{'{PARENT} 토큰 사용 — 부모별 미리보기는 수정 화면에서 확인'}</Muted>}
          </InfoValue>

          <InfoLabel>메모</InfoLabel>
          <InfoValue>{rule.description ?? <Muted />}</InfoValue>
        </InfoGrid>
      </FormSection>
    </>
  );
}
