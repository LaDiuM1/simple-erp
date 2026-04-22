import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { useAppSelector } from '@/app/hooks';
import { FormField, LoginCard, LoginContainer, LogoBox, StyledInput } from './LoginPage.styles';

export default function LoginPage() {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');

  if (accessToken) return <Navigate to="/" replace />;

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
  };

  return (
    <LoginContainer>
      <LoginCard>
        <Box sx={{ textAlign: 'center', mb: '2rem' }}>
          <LogoBox>ERP</LogoBox>
          <Typography sx={{ fontSize: '1.5rem', fontWeight: 700, color: 'text.primary', mb: '0.375rem' }}>
            SIMPLE ERP
          </Typography>
          <Typography sx={{ fontSize: '0.875rem', color: 'text.secondary' }}>
            업무 관리 시스템에 로그인하세요
          </Typography>
        </Box>

        <Box
          component="form"
          onSubmit={handleSubmit}
          sx={{ display: 'flex', flexDirection: 'column', gap: '1.125rem' }}
        >
          <FormField>
            <Typography
              component="label"
              htmlFor="loginId"
              sx={{ fontSize: '0.875rem', fontWeight: 500, color: 'text.primary' }}
            >
              아이디
            </Typography>
            <StyledInput
              id="loginId"
              type="text"
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)}
              placeholder="아이디를 입력하세요"
              required
              autoComplete="username"
              fullWidth
            />
          </FormField>

          <FormField>
            <Typography
              component="label"
              htmlFor="password"
              sx={{ fontSize: '0.875rem', fontWeight: 500, color: 'text.primary' }}
            >
              비밀번호
            </Typography>
            <StyledInput
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
              required
              autoComplete="current-password"
              fullWidth
            />
          </FormField>

          <Button
            type="submit"
            variant="contained"
            fullWidth
            sx={{
              mt: '0.25rem',
              py: '0.75rem',
              fontSize: '0.9375rem',
              fontWeight: 600,
              '&:active': { transform: 'scale(0.99)' },
            }}
          >
            로그인
          </Button>
        </Box>
      </LoginCard>
    </LoginContainer>
  );
}
