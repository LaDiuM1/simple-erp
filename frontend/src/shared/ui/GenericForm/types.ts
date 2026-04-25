import type { ReactNode } from 'react';

/* --------------------------------------------------------------------------
 * Query / Mutation 훅 시그니처 (RTK Query 호환)
 * ------------------------------------------------------------------------ */

export interface DetailQueryState<T> {
  data?: T;
  isLoading: boolean;
  isError: boolean;
  error?: unknown;
  refetch: () => void;
}

export type UseDetailQuery<TDetail> = (id: number) => DetailQueryState<TDetail>;

export type UseCreateMutation<TCreateRequest> = () => readonly [
  (body: TCreateRequest) => { unwrap: () => Promise<unknown> },
  { isLoading: boolean },
];

export type UseUpdateMutation<TUpdateRequest> = () => readonly [
  (arg: { id: number; body: TUpdateRequest }) => { unwrap: () => Promise<unknown> },
  { isLoading: boolean },
];

/* --------------------------------------------------------------------------
 * Field config (discriminated union)
 * ------------------------------------------------------------------------ */

export interface FieldOption<V extends string | number = string | number> {
  value: V;
  label: string;
}

interface BaseField<TValues> {
  key: keyof TValues & string;
  label: string;
  required?: boolean;
  helperText?: string;
  /** 생성 모드에서 숨김 (예: 읽기 전용 생성시간 필드) */
  hideOnCreate?: boolean;
  /** 수정 모드에서 숨김 (예: 로그인 ID / 비밀번호) */
  hideOnEdit?: boolean;
  /** 그리드에서 한 줄을 전부 차지 (기본: 반 칸) */
  fullWidth?: boolean;
}

export interface TextFieldConfig<TValues> extends BaseField<TValues> {
  type: 'text';
  maxLength?: number;
  placeholder?: string;
}

export interface PasswordFieldConfig<TValues> extends BaseField<TValues> {
  type: 'password';
  minLength?: number;
  placeholder?: string;
}

export interface EmailFieldConfig<TValues> extends BaseField<TValues> {
  type: 'email';
  placeholder?: string;
}

export interface PhoneFieldConfig<TValues> extends BaseField<TValues> {
  type: 'phone';
  placeholder?: string;
}

export interface DateFieldConfig<TValues> extends BaseField<TValues> {
  type: 'date';
}

export interface SelectFieldConfig<TValues> extends BaseField<TValues> {
  type: 'select';
  options?: FieldOption[];
  useOptions?: () => { data?: unknown };
  mapOptions?: (data: unknown) => FieldOption[];
}

export interface CustomFieldConfig<TValues> extends BaseField<TValues> {
  type: 'custom';
  render: (ctx: { value: unknown; onChange: (value: unknown) => void }) => ReactNode;
}

export type FieldConfig<TValues> =
  | TextFieldConfig<TValues>
  | PasswordFieldConfig<TValues>
  | EmailFieldConfig<TValues>
  | PhoneFieldConfig<TValues>
  | DateFieldConfig<TValues>
  | SelectFieldConfig<TValues>
  | CustomFieldConfig<TValues>;

/* --------------------------------------------------------------------------
 * API config
 * ------------------------------------------------------------------------ */

export interface FormTitles {
  create?: string;
  edit?: string;
}

/** 저장 성공 시 스낵바로 노출할 메시지. 미지정 시 공용 기본 문구 사용. */
export interface FormSuccessMessages {
  create?: string;
  edit?: string;
}

export interface FormApiConfig<
  TValues extends object,
  TDetail,
  TCreateRequest,
  TUpdateRequest,
> {
  /** 권한 체크 기준 (저장 버튼 PermissionGate) */
  menuCode: string;

  /** 수정 모드: 상세 조회 훅 */
  useGet: UseDetailQuery<TDetail>;
  /** 생성 뮤테이션 훅 */
  useCreate: UseCreateMutation<TCreateRequest>;
  /** 수정 뮤테이션 훅 */
  useUpdate: UseUpdateMutation<TUpdateRequest>;

  /** 생성 모드 초기값 */
  emptyValues: TValues;

  /** 서버 상세 → 폼 값 변환 (수정 모드 초기값) */
  toValues: (detail: TDetail) => TValues;
  /** 폼 값 → 생성 요청 body */
  toCreateRequest: (values: TValues) => TCreateRequest;
  /** 폼 값 → 수정 요청 body */
  toUpdateRequest: (values: TValues) => TUpdateRequest;

  /** 취소/성공 시 이동할 목록 경로 */
  listPath: string;

  /** 페이지 헤더 타이틀. 미지정 시 label 미노출 (AppLayout 이 라우트 매핑에서 결정) */
  titles?: FormTitles;

  /** 저장 성공 토스트 메시지 오버라이드 */
  successMessages?: FormSuccessMessages;
}

/* --------------------------------------------------------------------------
 * Form state (useFormState 반환)
 * ------------------------------------------------------------------------ */

export interface FormState<TValues> {
  values: TValues;
  updateField: <K extends keyof TValues>(key: K, value: TValues[K]) => void;
  setAll: (values: TValues) => void;
  reset: () => void;
}
