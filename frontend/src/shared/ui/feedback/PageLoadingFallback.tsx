import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import LoadingSpinner from './LoadingSpinner';

export default function PageLoadingFallback() {
  return (
    <Box
      role="status"
      aria-label="페이지를 불러오는 중"
      aria-live="polite"
      sx={{
        display: 'flex',
        minHeight: { xs: '16rem', sm: '20rem' },
        width: '100%',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
      }}
    >
      <LoadingSpinner />
      <Typography variant="body2" color="text.secondary">
        페이지를 불러오는 중입니다.
      </Typography>
    </Box>
  );
}
