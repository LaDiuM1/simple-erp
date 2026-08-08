import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';
import ErrorOutlineRoundedIcon from '@mui/icons-material/ErrorOutlineRounded';
import { useDemo } from '@/shared/demo/DemoContext';
import { DemoBadge, MaintenanceCard, MaintenanceRoot } from './DemoSurface.styles';

export default function DemoMaintenanceScreen() {
  const demo = useDemo();
  const statusUnavailable = demo.statusUnavailable;
  const failed = demo.failed;
  const title = statusUnavailable
    ? '환경 상태를 확인하고 있어요'
    : failed
      ? '데모 환경을 점검하고 있어요'
      : '데모 데이터를 초기화하고 있어요';
  const description = statusUnavailable
    ? '상태 제어면에 연결되지 않아 일반 화면을 안전하게 잠갔습니다. 연결이 복구되면 자동으로 다시 확인합니다.'
    : failed
    ? '정상 seed 복원과 검증이 완료될 때까지 환경을 열지 않습니다. 잠시 후 다시 확인해 주세요.'
    : '합성 DB와 파일을 기준 상태로 복원한 뒤 대표 기능을 검증하고 있습니다. 이 화면은 준비가 끝나면 자동으로 갱신됩니다.';
  const showError = failed || statusUnavailable;

  return (
    <MaintenanceRoot>
      <MaintenanceCard>
        {showError
          ? <ErrorOutlineRoundedIcon color="error" sx={{ fontSize: 42, mb: 1.25 }} />
          : <CircularProgress size={38} sx={{ mb: 1.5 }} />}
        <div><DemoBadge>데모</DemoBadge></div>
        <Typography
          role={showError ? 'alert' : 'status'}
          sx={{ mt: 1.5, fontSize: '1.25rem', fontWeight: 700 }}
        >
          {title}
        </Typography>
        <Typography sx={{ mt: 0.875, color: 'text.secondary', fontSize: '0.875rem', lineHeight: 1.65 }}>
          {description}
        </Typography>
        {!showError && (
          <Typography sx={{ mt: 1.5, color: 'text.secondary', fontSize: '0.8125rem' }}>
            상태를 5초마다 자동으로 확인하고 있어요.
          </Typography>
        )}
      </MaintenanceCard>
    </MaintenanceRoot>
  );
}
