import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { useLoginMutation } from '@/features/auth/api/authApi';
import { performLogin } from '@/features/auth/store/authActions';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { getErrorMessage } from '@/shared/api/error';
import type { DemoPublicAccount } from '@/shared/demo/demoContract';

/**
 * 로그인 page hook — 이미 로그인된 상태 신호 / 입력값 / 에러 / submit 핸들러 묶음.
 */
export function useLoginPage() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const snackbar = useSnackbar();
  const accessToken = useAppSelector((s) => s.auth.accessToken);

  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');

  const [login, { isLoading, error, reset }] = useLoginMutation();
  const errorMessage = getErrorMessage(error) ?? null;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    const result = await login({ loginId, password });
    if ('data' in result && result.data) {
      dispatch(performLogin(result.data.accessToken));
      snackbar.success('로그인되었습니다.');
      navigate('/');
    }
  };

  const fillCredentials = (account: DemoPublicAccount) => {
    setLoginId(account.loginId);
    setPassword(account.password);
    reset();
  };

  return {
    isAuthenticated: !!accessToken,
    loginId,
    setLoginId,
    password,
    setPassword,
    isLoading,
    errorMessage,
    fillCredentials,
    handleSubmit,
  };
}
