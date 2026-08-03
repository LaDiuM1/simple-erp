import type { Engineer } from '@/features/afterService/types';

export function eligibleEngineerOptions(
  engineers: Engineer[],
  currentEngineerId: number | string | null | undefined,
): Engineer[] {
  const currentId = currentEngineerId == null || currentEngineerId === ''
    ? null
    : Number(currentEngineerId);
  return engineers.filter((engineer) => engineer.active || engineer.id === currentId);
}
