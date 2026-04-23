import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

interface Props {
  /** 좌측 점 색상 (hex / css color). 도메인별 상태 매핑은 호출측에서 제공. */
  color: string;
  /** 우측 라벨 텍스트 */
  label: string;
}

/**
 * 상태 표시를 위한 프리미티브: 작은 원형 점 + 라벨 인라인 조합.
 * 도메인 의미 없음 — 호출측이 상태→색/라벨 매핑을 담당.
 */
export default function StatusDot({ color, label }: Props) {
  return (
    <Stack direction="row" alignItems="center" spacing={1}>
      <Box
        sx={{
          width: 7,
          height: 7,
          borderRadius: '50%',
          backgroundColor: color,
          flexShrink: 0,
        }}
      />
      <Typography sx={{ fontSize: '0.875rem', color: 'text.primary' }}>
        {label}
      </Typography>
    </Stack>
  );
}
