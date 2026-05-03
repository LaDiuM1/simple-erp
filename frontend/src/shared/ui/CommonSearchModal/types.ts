import type { ReactNode } from 'react';
import type {
  ColumnConfig,
  FilterConfig,
  ListQueryParamsBase,
  QueryState,
} from '@/shared/ui/GenericList';

/** 모달이 부모로 반환하는 단일 항목. id 는 폼 값으로, label 은 표시용으로 사용. */
export interface CommonSearchSelectedItem {
  id: number;
  label: string;
}

/** 모달이 검색할 도메인의 데이터 소스 + 행 → (id, label) 매핑. */
export interface CommonSearchModalApi<TRow, TFilters extends object> {
  /** 페이징/정렬/필터 파라미터로 호출되는 RTK Query 훅. */
  useList: (params: TFilters & ListQueryParamsBase) => QueryState<TRow>;
  /** 행 → 폼으로 넘어갈 id 값. */
  rowKey: (row: TRow) => number;
  /**
   * 행 → 트리거에 표시될 라벨 (선택 결과로 부모에 함께 반환됨).
   * select 모드에서만 사용 — 미지정 시 `String(id)` fallback.
   */
  rowLabel?: (row: TRow) => string;
  /** 모달 내 페이지 사이즈. default 10. */
  pageSize?: number;
}

interface BaseProps<TRow, TFilters extends object> {
  open: boolean;
  onClose: () => void;
  title: string;
  api: CommonSearchModalApi<TRow, TFilters>;
  /** 리스트 필터바 (search / select / custom) — GenericList 와 동일 구성. 미지정 시 필터바 숨김. */
  searchFilter?: FilterConfig[];
  /** 행에 표시할 컬럼 — GenericList 와 동일 구성. */
  column: ColumnConfig<TRow>[];
  /** 빈 결과 메시지 오버라이드. */
  emptyMessage?: string;
  /** 페이지네이션 영역 숨김 — 적은 항목 (~20) 의 sub-master 관리에 적합. */
  hidePagination?: boolean;
  /** DialogTitle 우측에 노출할 액션 (예: "추가" 버튼). */
  headerActions?: ReactNode;
}

/** 행을 선택해 부모로 반환하는 모달 (단일 / 다중 선택). */
export interface CommonSearchModalProps<TRow, TFilters extends object>
  extends BaseProps<TRow, TFilters> {
  /** true 면 다중 선택, false 면 단일 선택 (한 행만 유지). */
  multiple?: boolean;
  /**
   * 다중 선택 시 시각 / 상호작용 패턴.
   *  - 'checkbox' (default): 행마다 체크박스 — 익숙한 다중 토글.
   *  - 'tray': 행 클릭 → 하단 트레이에 칩으로 누적, 칩 X 로 제거.
   * `multiple: false` 면 무시 (단일 선택은 항상 라디오).
   */
  selectionStyle?: 'checkbox' | 'tray';
  /**
   * 확인 클릭 시 부모로 선택 결과 전달.
   * 단일 선택이면 길이 0 또는 1, 다중 선택이면 0개 이상.
   */
  onSelect: (selected: CommonSearchSelectedItem[]) => void;
  /** 모달 열릴 때 미리 체크해둘 항목 (수정 폼에서 현재 값 미리 표시 등). */
  initialSelected?: CommonSearchSelectedItem[];
  /** 결과에서 제외할 id 목록 (예: 부서 자기 자신을 상위 부서로 못 고르도록). */
  excludeIds?: number[];
  /** 확인 버튼 라벨 — 기본 '확인'. */
  confirmLabel?: string;
  /**
   * 'tray' selectionStyle 전용 — 트레이 칩의 커스텀 렌더링.
   * 미지정 시 기본 Chip + delete 사용.
   */
  renderTrayItem?: (
    item: CommonSearchSelectedItem,
    onRemove: () => void,
  ) => ReactNode;
}

/** 행 단위 액션 (예: 삭제) 만 노출하는 관리 모달 — 선택 / 확인 단계 없음. */
export interface CommonManageModalProps<TRow, TFilters extends object>
  extends BaseProps<TRow, TFilters> {
  /** 행마다 우측에 노출되는 액션 — 보통 삭제 IconButton. */
  rowActions: (row: TRow) => ReactNode;
}
