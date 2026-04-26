import type { RoleFormValues } from '@/features/role/types';

export type RoleField = 'code' | 'name';

export type RoleErrors = Partial<Record<RoleField, string>>;

const CODE_PATTERN = /^[A-Z][A-Z0-9_]{0,49}$/;

export function validateRoleCode(value: string, mode: 'create' | 'edit'): string | undefined {
  if (mode === 'edit') return undefined; // 코드는 수정 불가
  const v = value.trim();
  if (v === '') return '권한 코드를 입력해주세요.';
  if (!CODE_PATTERN.test(v)) {
    return '권한 코드는 영문 대문자로 시작하고, 영문 대문자/숫자/_ 만 사용 가능합니다 (최대 50자).';
  }
  return undefined;
}

export function validateRoleName(value: string): string | undefined {
  const v = value.trim();
  if (v === '') return '권한명을 입력해주세요.';
  if (v.length > 100) return '권한명은 100자 이내로 입력해주세요.';
  return undefined;
}

export function validateRoleForm(values: RoleFormValues, mode: 'create' | 'edit'): RoleErrors {
  return {
    code: validateRoleCode(values.code, mode),
    name: validateRoleName(values.name),
  };
}

export function hasErrors(errors: RoleErrors): boolean {
  return Object.values(errors).some((e) => e !== undefined);
}
