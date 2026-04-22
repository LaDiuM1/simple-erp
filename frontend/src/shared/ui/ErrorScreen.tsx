import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { ScreenContainer } from './LoadingScreen.styles';
import { ErrorIconBox } from './ErrorScreen.styles';

interface Props {
  message?: string;
  onRetry?: () => void;
}

export default function ErrorScreen({ message = '오류가 발생했습니다.', onRetry }: Props) {
  return (
    <ScreenContainer>
      <ErrorIconBox>!</ErrorIconBox>
      <Typography color="text.secondary" sx={{ fontSize: '0.9375rem', maxWidth: 360, lineHeight: 1.6 }}>
        {message}
      </Typography>
      {onRetry && (
        <Button
          variant="outlined"
          onClick={onRetry}
          sx={{
            mt: '0.25rem',
            px: '1.25rem',
            py: '0.5rem',
            fontSize: '0.875rem',
            fontWeight: 500,
            color: 'text.secondary',
            borderColor: 'divider',
            '&:hover': {
              borderColor: 'primary.main',
              color: 'primary.main',
              backgroundColor: (theme) => theme.palette.primarySubtle,
            },
          }}
        >
          다시 시도
        </Button>
      )}
    </ScreenContainer>
  );
}
