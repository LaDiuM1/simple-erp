import type { ReactNode } from 'react';
import Typography from '@mui/material/Typography';

interface Props {
  /** 기본값 '-' — null/empty placeholder 용도에 맞춤 */
  children?: ReactNode;
}

/**
 * 흐린 색(text.disabled) 의 인라인 텍스트.
 * 주 용도는 목록 셀에서 null/빈 값을 나타내는 placeholder('-') 이며,
 * children 을 주면 임의 텍스트를 같은 톤으로 렌더한다.
 */
export default function Muted({ children = '-' }: Props) {
  return (
    <Typography component="span" sx={{ color: 'text.disabled' }}>
      {children}
    </Typography>
  );
}
