import { Navigate } from 'react-router-dom';
import Collapse from '@mui/material/Collapse';
import { useLoginPage } from '@/features/auth/hooks/useLoginPage';
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
  const {
    isAuthenticated,
    loginId,
    setLoginId,
    password,
    setPassword,
    isLoading,
    errorMessage,
    handleSubmit,
  } = useLoginPage();

  if (isAuthenticated) return <Navigate to="/" replace />;

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
