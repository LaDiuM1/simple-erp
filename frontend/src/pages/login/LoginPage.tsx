import { Navigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { useAppSelector } from '@/app/hooks';
import { LoginCard, LoginContainer, LogoBox } from './LoginPage.styles';

export default function LoginPage() {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  if (accessToken) return <Navigate to="/" replace />;

  return (
    <LoginContainer>
      <LoginCard>
        <Box sx={{ textAlign: 'center' }}>
          <LogoBox>ERP</LogoBox>
          <Typography sx={{ fontSize: '1.5rem', fontWeight: 700, color: 'text.primary' }}>
            SIMPLE ERP
          </Typography>
        </Box>
      </LoginCard>
    </LoginContainer>
  );
}
