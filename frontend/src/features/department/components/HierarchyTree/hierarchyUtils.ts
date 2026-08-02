import type { DepartmentInfo } from '@/features/reference/types';

export function buildChildrenMap(
  depts: DepartmentInfo[],
): Map<number | null, DepartmentInfo[]> {
  const map = new Map<number | null, DepartmentInfo[]>();
  for (const dept of depts) {
    const key = dept.parentId ?? null;
    const list = map.get(key) ?? [];
    list.push(dept);
    map.set(key, list);
  }
  for (const list of map.values()) {
    list.sort((a, b) => a.code.localeCompare(b.code));
  }
  return map;
}

export function isDescendant(
  ancestorId: number,
  candidateId: number,
  childrenByParent: Map<number | null, DepartmentInfo[]>,
): boolean {
  const stack: number[] = [ancestorId];
  while (stack.length > 0) {
    const id = stack.pop() as number;
    const children = childrenByParent.get(id) ?? [];
    for (const child of children) {
      if (child.id === candidateId) return true;
      stack.push(child.id);
    }
  }
  return false;
}
