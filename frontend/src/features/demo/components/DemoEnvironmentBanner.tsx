import ErrorOutlineRoundedIcon from '@mui/icons-material/ErrorOutlineRounded';
import ScienceOutlinedIcon from '@mui/icons-material/ScienceOutlined';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import { useDemo } from '@/shared/demo/DemoContext';
import { BannerMessage, BannerMeta, BannerRoot, DemoBadge } from './DemoSurface.styles';

export default function DemoEnvironmentBanner() {
  const demo = useDemo();
  if (!demo.status.enabled) return null;

  const tone = demo.failed ? 'error' : demo.resetSoon || demo.writeLocked ? 'warning' : 'info';
  const Icon = demo.failed
    ? ErrorOutlineRoundedIcon
    : demo.resetSoon || demo.writeLocked
      ? WarningAmberRoundedIcon
      : ScienceOutlinedIcon;
  const message = demo.failed
    ? '데모 복원 검증 중 문제가 발생해 점검 중입니다.'
    : demo.writeLocked
      ? '초기화 준비로 변경이 잠시 중단되었습니다.'
      : '모든 정보는 합성 데이터입니다. 실제 개인정보나 파일을 입력하지 마세요.';

  return (
    <BannerRoot tone={tone}>
      <BannerMessage role={tone === 'error' ? 'alert' : 'status'}>
        <Icon sx={{ fontSize: 18, flexShrink: 0 }} />
        <strong>데모</strong>
        <span>{message}</span>
      </BannerMessage>
      <BannerMeta aria-live="off">
        <DemoBadge>{demo.resetSoon ? '초기화 임박' : '합성 데이터'}</DemoBadge>
        <span>다음 초기화 {demo.countdown}</span>
      </BannerMeta>
    </BannerRoot>
  );
}
