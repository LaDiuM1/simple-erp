import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import Muted from '@/shared/ui/atoms/Muted';
import { useGetCodeRuleQuery } from '@/features/codeRule/api/codeRuleApi';
import {
  CODE_RULE_TARGET_LABEL,
  INPUT_MODE_LABEL,
  type CodeRule,
  type CodeRuleTarget,
} from '@/features/codeRule/types';
import { Mono } from '@/pages/codeRule/CodeRuleDetailPage.styles';

/**
 * 코드 채번 규칙 상세 page hook — fetching / 권한 / headerActions / pageTitle 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). ruleInfoFields 는 detail 보장 시점에 page 가 호출.
 */
export function useCodeRuleDetailPage(target: CodeRuleTarget) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.CODE_RULES);
  const detailQuery = useGetCodeRuleQuery(target);

  const headerActions: PageHeaderAction[] = [
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
  ];

  return {
    queries: { detail: detailQuery },
    headerActions,
    pageTitle: `${CODE_RULE_TARGET_LABEL[target]} 상세`,
  };
}

export function ruleInfoFields(rule: CodeRule): HeaderDetailField[] {
  return [
    { label: '대상', value: CODE_RULE_TARGET_LABEL[rule.target] },
    { label: '패턴', value: <Mono>{rule.pattern}</Mono> },
    { label: '입력 방식', value: INPUT_MODE_LABEL[rule.inputMode] },
    {
      label: '다음 코드',
      value: rule.nextCode ? (
        <Mono>{rule.nextCode}</Mono>
      ) : (
        <Muted>{'PARENT / 분류 토큰 사용 — 미리보기는 수정 화면에서 확인'}</Muted>
      ),
    },
    { label: '메모', value: rule.description, fullWidth: true },
  ];
}
