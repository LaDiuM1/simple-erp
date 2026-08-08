import type { DemoPublicAccount } from '@/shared/demo/demoContract';
import { useDemo } from '@/shared/demo/DemoContext';
import {
  AccountButton,
  AccountDescription,
  AccountLabel,
  AccountList,
  AccountLoginId,
  AccountText,
  CountdownRow,
  CountdownValue,
  DemoBadge,
  DemoLoginRoot,
  DemoNotice,
  DemoTitle,
  DemoTitleRow,
} from './DemoSurface.styles';

export default function DemoLoginCard({
  onFill,
}: {
  onFill: (account: DemoPublicAccount) => void;
}) {
  const demo = useDemo();
  if (!demo.status.enabled) return null;

  const unavailable = demo.maintenance || demo.failed;
  return (
    <DemoLoginRoot aria-label="데모 계정">
      <DemoTitleRow>
        <DemoTitle>데모</DemoTitle>
        <DemoBadge>합성 데이터</DemoBadge>
      </DemoTitleRow>
      <DemoNotice>
        {'모든 정보는 합성 데이터이며 주기적으로 초기화됩니다.\n실제 개인정보나 파일을 입력하지 마세요.'}
      </DemoNotice>

      <AccountList>
        {demo.status.publicAccounts.map((account) => (
          <AccountButton
            key={account.loginId}
            type="button"
            variant="outlined"
            disabled={unavailable}
            onClick={() => onFill(account)}
          >
            <AccountText>
              <AccountLabel>
                {account.label}{account.recommended ? ' (추천)' : ''}
              </AccountLabel>
              <AccountDescription>{account.description}</AccountDescription>
            </AccountText>
            <AccountLoginId>{account.loginId}</AccountLoginId>
          </AccountButton>
        ))}
      </AccountList>

      <CountdownRow>
        <span>{unavailable ? '현재 상태' : '다음 초기화까지'}</span>
        <CountdownValue>
          {demo.failed ? '점검 중' : demo.maintenance ? '초기화 중' : demo.countdown}
        </CountdownValue>
      </CountdownRow>
    </DemoLoginRoot>
  );
}
