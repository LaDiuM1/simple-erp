import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { useGetRoleQuery } from '@/features/role/api/roleApi';
import { useRoleEditForm } from '@/features/role/hooks/useRoleEditForm';
import RoleForm from '@/features/role/components/RoleForm';
import type { RoleDetail } from '@/features/role/types';
import type { ApiError } from '@/shared/types/api';

interface Props {
  id: number;
}

/**
 * 데이터 fetch + 로딩/에러 분기는 outer 가, 폼 상태는 Body 가.
 * Department/CodeRule EditForm 의 outer + Body 분해 패턴을 따른다.
 */
export default function RoleEditForm({ id }: Props) {
  const { data, isLoading, isError, error, refetch } = useGetRoleQuery(id);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  if (!data) return null;

  return <Body id={id} detail={data} />;
}

function Body({ id, detail }: { id: number; detail: RoleDetail }) {
  const form = useRoleEditForm(id, detail);
  return <RoleForm mode="edit" {...form} />;
}
