import Checkbox from '@mui/material/Checkbox';
import { MENU_CODE, MENU_LABEL, type MenuCode } from '@/shared/config/menuConfig';
import {
  Banner,
  ColumnCenter,
  HeaderColumnCenter,
  HeaderToggleAll,
  MatrixHeader,
  MatrixRoot,
  MatrixRow,
  MenuLabel,
} from './MenuPermissionMatrix.styles';
import type { RoleFormValues } from '@/features/role/types';

/** 매트릭스 행 순서 — MENU_CODE 선언 순서를 따른다. */
export const MATRIX_MENUS: MenuCode[] = Object.values(MENU_CODE);

interface Props {
  permissions: RoleFormValues['permissions'];
  onChange: (next: RoleFormValues['permissions']) => void;
  readOnly?: boolean;
  /**
   * readOnly 시 노출되는 안내 배너 문구. **미지정 시 배너 미노출** —
   * 비활성 회색 체크박스만으로도 "변경 불가" 의미가 충분히 전달되므로
   * 시스템 권한처럼 추가 설명이 필요한 경우에만 명시 전달.
   */
  readOnlyMessage?: string;
}

/**
 * 메뉴별 (읽기/쓰기) 매트릭스 편집 위젯.
 * - 쓰기 체크 시 읽기 자동 체크 / 읽기 해제 시 쓰기 자동 해제 (의미적 정합)
 * - 컬럼 헤더의 "전체" 버튼으로 일괄 토글
 * - readOnly 일 때 모든 입력 비활성 + 안내 배너
 */
export default function MenuPermissionMatrix({
  permissions,
  onChange,
  readOnly = false,
  readOnlyMessage,
}: Props) {
  const updateOne = (
    menu: MenuCode,
    next: { canRead: boolean; canWrite: boolean },
  ) => {
    onChange({ ...permissions, [menu]: next });
  };

  const setRead = (menu: MenuCode, checked: boolean) => {
    const current = permissions[menu] ?? { canRead: false, canWrite: false };
    if (checked) {
      updateOne(menu, { canRead: true, canWrite: current.canWrite });
    } else {
      // 읽기 해제 시 쓰기도 자동 해제
      updateOne(menu, { canRead: false, canWrite: false });
    }
  };

  const setWrite = (menu: MenuCode, checked: boolean) => {
    if (checked) {
      // 쓰기 체크 시 읽기 자동 체크
      updateOne(menu, { canRead: true, canWrite: true });
    } else {
      const current = permissions[menu] ?? { canRead: false, canWrite: false };
      updateOne(menu, { canRead: current.canRead, canWrite: false });
    }
  };

  const toggleAllRead = () => {
    const allOn = MATRIX_MENUS.every((m) => permissions[m]?.canRead);
    const next = { ...permissions };
    for (const m of MATRIX_MENUS) {
      const cur = next[m] ?? { canRead: false, canWrite: false };
      if (allOn) {
        next[m] = { canRead: false, canWrite: false }; // 읽기 해제 → 쓰기도 해제
      } else {
        next[m] = { canRead: true, canWrite: cur.canWrite };
      }
    }
    onChange(next);
  };

  const toggleAllWrite = () => {
    const allOn = MATRIX_MENUS.every((m) => permissions[m]?.canWrite);
    const next = { ...permissions };
    for (const m of MATRIX_MENUS) {
      if (allOn) {
        const cur = next[m] ?? { canRead: false, canWrite: false };
        next[m] = { canRead: cur.canRead, canWrite: false };
      } else {
        next[m] = { canRead: true, canWrite: true }; // 쓰기 체크 → 읽기도 체크
      }
    }
    onChange(next);
  };

  const allReadOn = MATRIX_MENUS.every((m) => permissions[m]?.canRead);
  const allWriteOn = MATRIX_MENUS.every((m) => permissions[m]?.canWrite);

  return (
    <>
      {readOnly && readOnlyMessage && <Banner>{readOnlyMessage}</Banner>}
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
        </MatrixHeader>

        {MATRIX_MENUS.map((menu) => {
          const p = permissions[menu] ?? { canRead: false, canWrite: false };
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
            </MatrixRow>
          );
        })}
      </MatrixRoot>
    </>
  );
}
