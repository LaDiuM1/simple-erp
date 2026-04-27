import { useEffect, useState } from 'react';
import VerifiedUserRoundedIcon from '@mui/icons-material/VerifiedUserRounded';
import type { EmployeeProfileResponse } from '@/features/employee/types';
import {
  ClockDate,
  ClockText,
  HeroBadge,
  HeroDivider,
  HeroEyebrow,
  HeroGreeting,
  HeroLeft,
  HeroRight,
  HeroRoot,
  HeroSubtext,
} from './HeroBanner.styles';
import { formatClockTime, formatTodayLong } from '../../utils/formatters';

interface Props {
  profile: EmployeeProfileResponse;
}

export default function HeroBanner({ profile }: Props) {
  const now = useNow();

  return (
    <HeroRoot>
      <HeroLeft>
        <HeroEyebrow>WELCOME BACK</HeroEyebrow>
        <HeroGreeting>
          안녕하세요, <strong>{profile.name}</strong> 님
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
        <ClockText>{formatClockTime(now)}</ClockText>
        <ClockDate>{formatTodayLong(now)}</ClockDate>
      </HeroRight>
    </HeroRoot>
  );
}

function useNow(): Date {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const id = window.setInterval(() => setNow(new Date()), 1000);
    return () => window.clearInterval(id);
  }, []);
  return now;
}
