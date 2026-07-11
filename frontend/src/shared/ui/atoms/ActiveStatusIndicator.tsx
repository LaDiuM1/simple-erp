import { useTheme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';

/**
 * 사용 여부 (active boolean) 전용 텍스트 표시 — 공급사 / 제품 모델 등 마스터 목록에서 공용.
 * GenericTabbedTable 컨벤션과 동일하게 시각 차등은 텍스트 색만 — dot/chip/badge 미사용.
 */
export default function ActiveStatusIndicator({ active }: { active: boolean }) {
  const theme = useTheme();
  return (
    <Typography
      component="span"
      sx={{
        fontSize: '0.875rem',
        color: active ? theme.palette.statusActive : theme.palette.text.disabled,
        fontWeight: 500,
      }}
    >
      {active ? '사용' : '미사용'}
    </Typography>
  );
}
