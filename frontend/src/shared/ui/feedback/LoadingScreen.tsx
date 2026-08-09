import Typography from '@mui/material/Typography';
import LoadingSpinner from './LoadingSpinner';
import { ScreenContainer } from './LoadingScreen.styles';

interface Props {
  fullScreen?: boolean;
}

export default function LoadingScreen({ fullScreen = true }: Props) {
  return (
    <ScreenContainer
      role="status"
      aria-live="polite"
      aria-label="내용 불러오는 중"
      sx={
        !fullScreen
          ? { minHeight: '400px', height: '100%', backgroundColor: 'transparent' }
          : undefined
      }
    >
      <LoadingSpinner />
      <Typography variant="body2" color="text.secondary">
        불러오는 중...
      </Typography>
    </ScreenContainer>
  );
}
