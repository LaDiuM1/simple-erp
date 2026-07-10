import EventAvailableRoundedIcon from '@mui/icons-material/EventAvailableRounded';
import EventBusyRoundedIcon from '@mui/icons-material/EventBusyRounded';
import BeachAccessRoundedIcon from '@mui/icons-material/BeachAccessRounded';
import type { LeaveBalance } from '@/features/attendance/types';
import {
  BalanceGrid,
  BalanceTile,
  TileBody,
  TileIcon,
  TileLabel,
  TileSuffix,
  TileUnit,
  TileValue,
  TileValueRow,
} from './LeaveBalanceCard.styles';

interface Props {
  balance: LeaveBalance;
}

/** 잔여 연차 카드 — 부여 / 사용 / 잔여 3개 타일 (대시보드 KpiCard 톤). */
export default function LeaveBalanceCard({ balance }: Props) {
  return (
    <BalanceGrid>
      <BalanceTile>
        <TileIcon><EventAvailableRoundedIcon /></TileIcon>
        <TileBody>
          <TileLabel>부여 연차</TileLabel>
          <TileValueRow>
            <TileValue>{balance.grantedDays}</TileValue>
            <TileUnit>일</TileUnit>
          </TileValueRow>
          <TileSuffix>{balance.year}년 기준</TileSuffix>
        </TileBody>
      </BalanceTile>

      <BalanceTile>
        <TileIcon><EventBusyRoundedIcon /></TileIcon>
        <TileBody>
          <TileLabel>사용 연차</TileLabel>
          <TileValueRow>
            <TileValue>{balance.usedDays}</TileValue>
            <TileUnit>일</TileUnit>
          </TileValueRow>
        </TileBody>
      </BalanceTile>

      <BalanceTile>
        <TileIcon><BeachAccessRoundedIcon /></TileIcon>
        <TileBody>
          <TileLabel>잔여 연차</TileLabel>
          <TileValueRow>
            <TileValue>{balance.remainingDays}</TileValue>
            <TileUnit>일</TileUnit>
          </TileValueRow>
        </TileBody>
      </BalanceTile>
    </BalanceGrid>
  );
}
