import CalendarTodayRoundedIcon from '@mui/icons-material/CalendarTodayRounded';
import VerifiedUserRoundedIcon from '@mui/icons-material/VerifiedUserRounded';
import type { EmployeeProfileResponse } from '@/features/employee/types';
import { useToday } from '@/shared/hooks/useToday';
import {
  DateLabel,
  DateMeta,
  HeroBadge,
  HeroDivider,
  HeroGreeting,
  HeroLeft,
  HeroRight,
  HeroRoot,
  HeroSubtext,
} from './HeroBanner.styles';
import { formatTodayLong } from '../../utils/formatters';

interface Props {
  profile: EmployeeProfileResponse;
}

export default function HeroBanner({ profile }: Props) {
  const today = useToday();

  return (
    <HeroRoot>
      <HeroLeft>
        <HeroGreeting>
          <strong>{profile.name}</strong>님, 오늘의 업무 현황이에요.
        </HeroGreeting>
        <HeroSubtext>
          <HeroBadge>
            <VerifiedUserRoundedIcon sx={{ fontSize: 14 }} />
            {profile.roleName}
          </HeroBadge>
          {profile.departmentName && (
            <>
              <HeroDivider />
              <span>{profile.departmentName}</span>
            </>
          )}
          {profile.positionName && (
            <>
              <HeroDivider />
              <span>{profile.positionName}</span>
            </>
          )}
        </HeroSubtext>
      </HeroLeft>
      <HeroRight>
        <CalendarTodayRoundedIcon aria-hidden />
        <DateMeta>
          <DateLabel>오늘</DateLabel>
          <span>{formatTodayLong(today)}</span>
        </DateMeta>
      </HeroRight>
    </HeroRoot>
  );
}
