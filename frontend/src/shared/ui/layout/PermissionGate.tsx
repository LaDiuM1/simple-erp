import type { ReactNode } from 'react';
import { usePermission } from '@/shared/hooks/usePermission';

interface Props {
  menuCode: string;
  action?: 'read' | 'write';
  fallback?: ReactNode;
  children: ReactNode;
}

/**
 * 메뉴 권한이 없으면 children을 렌더링하지 않는다.
 * 등록/수정/삭제 같은 액션 버튼을 권한 기반으로 노출 제어할 때 사용.
 */
export default function PermissionGate({ menuCode, action = 'read', fallback = null, children }: Props) {
  const permission = usePermission(menuCode);
  const allowed = action === 'write' ? permission.canWrite : permission.canRead;
  return <>{allowed ? children : fallback}</>;
}
