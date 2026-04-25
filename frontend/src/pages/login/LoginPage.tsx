import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import Collapse from '@mui/material/Collapse';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { useLoginMutation } from '@/features/auth/api/authApi';
import { setToken } from '@/features/auth/store/authSlice';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { ApiError } from '@/shared/types/api';
import {
  AppSubtitle,
  AppTitle,
  ErrorBox,
  FieldLabel,
  FormField,
  LoginCard,
  LoginContainer,
  LoginForm,
  LoginHeader,
  LogoBox,
  StyledInput,
  SubmitButton,
} from './LoginPage.styles';

export default function LoginPage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const snackbar = useSnackbar();
  const accessToken = useAppSelector((s) => s.auth.accessToken);

  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');

  const [login, { isLoading, error }] = useLoginMutation();
  const errorMessage = error ? (error as ApiError).message : null;

  if (accessToken) return <Navigate to="/" replace />;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    const result = await login({ loginId, password });
    if ('data' in result && result.data) {
      dispatch(setToken(result.data.accessToken));
      snackbar.success('로그인되었습니다.');
      navigate('/');
    }
  };

  return (
    <LoginContainer>
      <LoginCard>
        <LoginHeader>
          <LogoBox>ERP</LogoBox>
          <AppTitle>SIMPLE ERP</AppTitle>
          <AppSubtitle>업무 관리 시스템에 로그인하세요</AppSubtitle>
        </LoginHeader>

        <LoginForm onSubmit={handleSubmit}>
          <FormField>
            <FieldLabel htmlFor="loginId">아이디</FieldLabel>
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
            <FieldLabel htmlFor="password">비밀번호</FieldLabel>
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

          <Collapse in={!!errorMessage} timeout={150}>
            <ErrorBox>{errorMessage}</ErrorBox>
          </Collapse>

          <SubmitButton type="submit" variant="contained" fullWidth disabled={isLoading}>
            {isLoading ? '로그인 중...' : '로그인'}
          </SubmitButton>
        </LoginForm>
      </LoginCard>
    </LoginContainer>
  );
}
