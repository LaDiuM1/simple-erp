import type { ReactNode } from 'react';
import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

interface Props {
  title: string;
  /** 우측 액션 슬롯. AppLayout이 ref div를 주입해 PageHeaderActions 포털 타깃으로 사용 */
  actions?: ReactNode;
}

/**
 * 모든 페이지 공통 sub-toolbar.
 * AppLayout 내부에서 라우트 handle.title 기반으로 자동 렌더된다.
 * 페이지는 직접 렌더하지 않고 PageHeaderActions로 액션만 주입.
 */
export default function PageHeader({ title, actions }: Props) {
  return (
    <Box
      sx={(theme) => ({
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '1rem',
        px: { xs: '1rem', sm: '1.5rem' },
        py: '0.75rem',
        backgroundColor: theme.palette.background.paper,
        borderBottom: `1px solid ${theme.palette.divider}`,
        boxShadow: '0 2px 6px -1px rgba(15, 23, 42, 0.05)',
        position: 'relative',
        zIndex: 1,
        flexShrink: 0,
        minHeight: 52,
      })}
    >
      <Typography
        sx={{
          fontSize: '1.0625rem',
          fontWeight: 700,
          color: 'text.primary',
          letterSpacing: '-0.01em',
          minWidth: 0,
        }}
      >
        {title}
      </Typography>
      <Stack direction="row" spacing={1} sx={{ flexShrink: 0, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
        {actions}
      </Stack>
    </Box>
  );
}
