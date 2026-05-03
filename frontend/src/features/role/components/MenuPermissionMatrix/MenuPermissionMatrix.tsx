import Checkbox from '@mui/material/Checkbox';
import MenuItem from '@mui/material/MenuItem';
import { MENU_CODE, MENU_LABEL, type MenuCode } from '@/shared/config/menuConfig';
import {
  ColumnCenter,
  HeaderColumnCenter,
  HeaderToggleAll,
  MatrixHeader,
  MatrixRoot,
  MatrixRow,
  MenuLabel,
  ScopeColumn,
  ScopeSelect,
} from './MenuPermissionMatrix.styles';
import {
  DATA_SCOPE_OPTIONS,
  type DataScope,
  type MenuPermissionFormValue,
  type RoleFormValues,
} from '@/features/role/types';

/** 매트릭스 행 순서 — MENU_CODE 선언 순서를 따른다. */
export const MATRIX_MENUS: MenuCode[] = Object.values(MENU_CODE);

const EMPTY_PERMISSION: MenuPermissionFormValue = { canRead: false, canWrite: false, dataScope: 'ALL' };

interface Props {
  permissions: RoleFormValues['permissions'];
  onChange: (next: RoleFormValues['permissions']) => void;
  readOnly?: boolean;
}

/**
 * 메뉴별 (읽기 / 쓰기 / 데이터 범위) 매트릭스 편집 위젯.
 * - 쓰기 체크 시 읽기 자동 체크 / 읽기 해제 시 쓰기 자동 해제 (의미적 정합)
 * - 컬럼 헤더의 "전체" 버튼으로 일괄 토글 (read / write)
 * - 데이터 범위는 read 권한이 있을 때만 활성 — 읽기 자체가 없으면 행 가시성도 무의미
 * - readOnly 일 때 모든 입력 비활성 (회색 체크박스로 변경 불가 의미 전달)
 */
export default function MenuPermissionMatrix({
  permissions,
  onChange,
  readOnly = false,
}: Props) {
  const updateOne = (menu: MenuCode, next: MenuPermissionFormValue) => {
    onChange({ ...permissions, [menu]: next });
  };

  const setRead = (menu: MenuCode, checked: boolean) => {
    const current = permissions[menu] ?? EMPTY_PERMISSION;
    if (checked) {
      updateOne(menu, { ...current, canRead: true });
    } else {
      // 읽기 해제 시 쓰기도 자동 해제 — dataScope 는 보존 (다시 켤 때 의도 유지)
      updateOne(menu, { ...current, canRead: false, canWrite: false });
    }
  };

  const setWrite = (menu: MenuCode, checked: boolean) => {
    const current = permissions[menu] ?? EMPTY_PERMISSION;
    if (checked) {
      // 쓰기 체크 시 읽기 자동 체크
      updateOne(menu, { ...current, canRead: true, canWrite: true });
    } else {
      updateOne(menu, { ...current, canWrite: false });
    }
  };

  const setScope = (menu: MenuCode, scope: DataScope) => {
    const current = permissions[menu] ?? EMPTY_PERMISSION;
    updateOne(menu, { ...current, dataScope: scope });
  };

  const toggleAllRead = () => {
    const allOn = MATRIX_MENUS.every((m) => permissions[m]?.canRead);
    const next = { ...permissions };
    for (const m of MATRIX_MENUS) {
      const cur = next[m] ?? EMPTY_PERMISSION;
      next[m] = allOn
        ? { ...cur, canRead: false, canWrite: false } // 읽기 해제 → 쓰기도 해제
        : { ...cur, canRead: true };
    }
    onChange(next);
  };

  const toggleAllWrite = () => {
    const allOn = MATRIX_MENUS.every((m) => permissions[m]?.canWrite);
    const next = { ...permissions };
    for (const m of MATRIX_MENUS) {
      const cur = next[m] ?? EMPTY_PERMISSION;
      next[m] = allOn
        ? { ...cur, canWrite: false }
        : { ...cur, canRead: true, canWrite: true }; // 쓰기 체크 → 읽기도 체크
    }
    onChange(next);
  };

  const allReadOn = MATRIX_MENUS.every((m) => permissions[m]?.canRead);
  const allWriteOn = MATRIX_MENUS.every((m) => permissions[m]?.canWrite);

  return (
    <MatrixRoot>
      <MatrixHeader>
        <span>메뉴</span>
        <HeaderColumnCenter>
          <span>읽기</span>
          <HeaderToggleAll
            type="button"
            onClick={toggleAllRead}
            disabled={readOnly}
          >
            {allReadOn ? '전체 해제' : '전체 선택'}
          </HeaderToggleAll>
        </HeaderColumnCenter>
        <HeaderColumnCenter>
          <span>쓰기</span>
          <HeaderToggleAll
            type="button"
            onClick={toggleAllWrite}
            disabled={readOnly}
          >
            {allWriteOn ? '전체 해제' : '전체 선택'}
          </HeaderToggleAll>
        </HeaderColumnCenter>
        <HeaderColumnCenter>
          <span>데이터 범위</span>
        </HeaderColumnCenter>
      </MatrixHeader>

      {MATRIX_MENUS.map((menu) => {
        const p = permissions[menu] ?? EMPTY_PERMISSION;
        const scopeDisabled = readOnly || !p.canRead;
        return (
          <MatrixRow key={menu} readOnly={readOnly}>
            <MenuLabel>{MENU_LABEL[menu]}</MenuLabel>
            <ColumnCenter>
              <Checkbox
                size="small"
                checked={p.canRead}
                disabled={readOnly}
                onChange={(e) => setRead(menu, e.target.checked)}
              />
            </ColumnCenter>
            <ColumnCenter>
              <Checkbox
                size="small"
                checked={p.canWrite}
                disabled={readOnly}
                onChange={(e) => setWrite(menu, e.target.checked)}
              />
            </ColumnCenter>
            <ScopeColumn>
              <ScopeSelect
                size="small"
                fullWidth
                value={p.dataScope}
                disabled={scopeDisabled}
                onChange={(e) => setScope(menu, e.target.value as DataScope)}
              >
                {DATA_SCOPE_OPTIONS.map((opt) => (
                  <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                ))}
              </ScopeSelect>
            </ScopeColumn>
          </MatrixRow>
        );
      })}
    </MatrixRoot>
  );
}
