import Typography from '@mui/material/Typography';

interface Props {
  withinRange: boolean;
}

/** 출퇴근 위치의 사무실 반경 내 / 밖 여부 — 텍스트 색으로만 구분 (chip / badge 미사용). */
export default function WithinRangeText({ withinRange }: Props) {
  return (
    <Typography
      component="span"
      sx={{
        fontSize: 'inherit',
        fontWeight: 500,
        color: withinRange ? 'success.main' : 'warning.main',
      }}
    >
      {withinRange ? '반경 내' : '반경 밖'}
    </Typography>
  );
}
