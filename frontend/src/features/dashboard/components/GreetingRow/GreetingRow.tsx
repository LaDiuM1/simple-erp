import Button from '@mui/material/Button';
import type { EmployeeProfileResponse } from '@/features/employee/types';
import { formatTodayLong } from '../../utils/formatters';
import { ActionArea, GreetingRoot, GreetingSub, GreetingTitle } from './GreetingRow.styles';

export interface QuickAction {
  label: string;
  onClick: () => void;
  primary?: boolean;
}

interface Props {
  profile: EmployeeProfileResponse;
  followUpCount: number;
  quickActions: QuickAction[];
}

/**
 * 인사말 한 줄 + 오늘 날짜 / 팔로업 요약 + 빠른 작업 버튼.
 * 기존 HeroBanner (그라데이션 카드 + 시계) 대체 — 콘텐츠가 아닌 장식 제거.
 */
export default function GreetingRow({ profile, followUpCount, quickActions }: Props) {
  const followUpText =
    followUpCount > 0 ? ` · 팔로업 ${followUpCount}건이 기다리고 있어요` : '';

  return (
    <GreetingRoot>
      <div>
        <GreetingTitle>안녕하세요, {profile.name} 님</GreetingTitle>
        <GreetingSub>
          {formatTodayLong(new Date())}
          {followUpText}
        </GreetingSub>
      </div>
      {quickActions.length > 0 && (
        <ActionArea>
          {quickActions.map((action) => (
            <Button
              key={action.label}
              variant={action.primary ? 'contained' : 'outlined'}
              size="small"
              onClick={action.onClick}
              sx={
                action.primary
                  ? undefined
                  : { color: 'text.primary', borderColor: 'divider' }
              }
            >
              {action.label}
            </Button>
          ))}
        </ActionArea>
      )}
    </GreetingRoot>
  );
}
