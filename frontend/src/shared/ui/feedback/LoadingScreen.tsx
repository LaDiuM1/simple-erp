import Typography from '@mui/material/Typography';
import LoadingSpinner from './LoadingSpinner';
import { ScreenContainer } from './LoadingScreen.styles';

export default function LoadingScreen() {
  return (
    <ScreenContainer>
      <LoadingSpinner />
      <Typography variant="body2" color="text.secondary">
        불러오는 중...
      </Typography>
    </ScreenContainer>
  );
}
