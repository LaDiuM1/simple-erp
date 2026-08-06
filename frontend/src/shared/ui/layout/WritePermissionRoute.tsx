import { Outlet } from 'react-router-dom';
import type { MenuCode } from '@/shared/config/menuConfig';
import { getErrorMessage } from '@/shared/api/error';
import { usePermissionBoundary } from '@/shared/hooks/usePermission';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import PageLoadingFallback from '@/shared/ui/feedback/PageLoadingFallback';

interface Props {
  menuCode: MenuCode;
}

/** 등록·수정 전용 URL 에 쓰기 권한이 없는 사용자가 직접 진입하지 못하게 하는 라우트 경계. */
export default function WritePermissionRoute({ menuCode }: Props) {
  const permission = usePermissionBoundary(menuCode);

  if (permission.isLoading) return <PageLoadingFallback />;

  if (permission.isError) {
    return (
      <ErrorScreen
        message={getErrorMessage(permission.error, '권한 정보를 불러오지 못했습니다.')}
        onRetry={permission.retry}
        fullScreen={false}
      />
    );
  }

  if (!permission.canWrite) {
    return (
      <ErrorScreen
        message="수정 권한이 없어 이 페이지를 사용할 수 없습니다."
        fullScreen={false}
      />
    );
  }

  return <Outlet />;
}
