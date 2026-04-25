import * as React from 'react';
import { useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import CircularProgress from '@mui/material/CircularProgress';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import AddIcon from '@mui/icons-material/Add';
import ApartmentRoundedIcon from '@mui/icons-material/ApartmentRounded';
import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import PlaceRoundedIcon from '@mui/icons-material/PlaceRounded';
import SearchRoundedIcon from '@mui/icons-material/SearchRounded';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { PrimaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useDaumPostcode } from '@/shared/hooks/useDaumPostcode';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import {
  useFieldValidation,
  type ValidatorMap,
} from '@/shared/hooks/useFieldValidation';
import { CancelHeaderButton } from '@/shared/ui/GenericForm/GenericForm.styles';
import {
  useCheckLoginIdAvailabilityQuery,
  useCreateEmployeeMutation,
} from '@/features/employee/api/employeeApi';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import {
  EMPTY_EMPLOYEE_FORM,
  employeeFormToCreateRequest,
  type EmployeeFormValues,
} from '@/features/employee/types';
import type { ApiError } from '@/shared/types/api';
import {
  AddressSearchButton,
  AddressSearchRow,
  CreateForm,
  CreateRoot,
  FieldCol2,
  FieldFull,
  FieldGrid,
  HeroAvatar,
  HeroAvatarPlaceholder,
  HeroBody,
  HeroCard,
  HeroEyebrow,
  HeroSubtitle,
  HeroTitle,
  SectionDescription,
  SectionHeader,
  SectionIcon,
  SectionSurface,
  SectionTitle,
  SectionTitleBox,
} from './EmployeeCreatePage.styles';

const FORM_ID = 'employee-create-form';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const LOGIN_ID_MIN = 3;

const VALIDATORS: ValidatorMap<EmployeeFormValues> = {
  loginId: (v) => {
    const trimmed = v.trim();
    if (trimmed === '') return '로그인 ID를 입력해주세요.';
    if (trimmed.length < LOGIN_ID_MIN) return `로그인 ID는 ${LOGIN_ID_MIN}자 이상이어야 합니다.`;
    return null;
  },
  password: (v) =>
    v.length < 4 ? '비밀번호는 4자 이상이어야 합니다.' : null,
  passwordConfirm: (v, all) =>
    v !== all.password ? '비밀번호가 일치하지 않습니다.' : null,
  name: (v) => (v.trim() === '' ? '이름을 입력해주세요.' : null),
  email: (v) =>
    v !== '' && !EMAIL_RE.test(v) ? '이메일 형식이 올바르지 않습니다.' : null,
  roleId: (v) => (v === '' ? '권한을 선택해주세요.' : null),
};

type LoginIdStatus = 'idle' | 'checking' | 'available' | 'taken';

/** 오늘 날짜를 input[type=date] 가 받는 YYYY-MM-DD 포맷으로 반환. 로컬 시간 기준. */
function todayIsoDate(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export default function EmployeeCreatePage() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const openPostcode = useDaumPostcode();
  const [values, setValues] = useState<EmployeeFormValues>(() => ({
    ...EMPTY_EMPLOYEE_FORM,
    joinDate: todayIsoDate(),
  }));
  const [createEmployee, { isLoading: isSaving }] = useCreateEmployeeMutation();

  const { data: roles } = useGetRolesQuery();
  const { data: departments } = useGetDepartmentsQuery();
  const { data: positions } = useGetPositionsQuery();

  const update = <K extends keyof EmployeeFormValues>(key: K, v: EmployeeFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const validation = useFieldValidation(values, VALIDATORS);

  // 로그인 ID 가용성 — 디바운스된 값으로 BE 조회
  const trimmedLoginId = values.loginId.trim();
  const debouncedLoginId = useDebouncedValue(trimmedLoginId, 400);
  const skipAvailabilityCheck =
    debouncedLoginId === '' || debouncedLoginId.length < LOGIN_ID_MIN;
  const { data: availability, isFetching: isCheckingLoginId } =
    useCheckLoginIdAvailabilityQuery(debouncedLoginId, { skip: skipAvailabilityCheck });

  const loginIdStatus: LoginIdStatus =
    skipAvailabilityCheck
      ? 'idle'
      : trimmedLoginId !== debouncedLoginId || isCheckingLoginId || !availability
        ? 'checking'
        : availability.available
          ? 'available'
          : 'taken';

  const handleAddressSearch = () => {
    openPostcode((data) => {
      update('zipCode', data.zonecode);
      update('roadAddress', data.roadAddress);
    }).catch((err: Error) => {
      snackbar.error(err.message);
    });
  };

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    if (loginIdStatus === 'checking') {
      snackbar.error('로그인 ID 가용성을 확인 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }
    if (loginIdStatus === 'taken') {
      snackbar.error('이미 사용 중인 로그인 ID 입니다.');
      return;
    }
    try {
      await createEmployee(employeeFormToCreateRequest(values)).unwrap();
      snackbar.success('등록되었습니다.');
      navigate(MENU_PATH.EMPLOYEES);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '저장 중 오류가 발생했습니다.');
    }
  };

  const initial = values.name.trim().charAt(0);

  return (
    <>
      <PageHeaderActions>
        <CancelHeaderButton
          type="button"
          variant="outlined"
          onClick={() => navigate(MENU_PATH.EMPLOYEES)}
          disabled={isSaving}
        >
          취소
        </CancelHeaderButton>
        <PermissionGate menuCode={MENU_CODE.EMPLOYEES} action="write">
          <PrimaryPageHeaderButton
            type="submit"
            form={FORM_ID}
            disabled={isSaving}
            startIcon={
              isSaving ? <CircularProgress size={14} color="inherit" /> : <AddIcon />
            }
          >
            등록
          </PrimaryPageHeaderButton>
        </PermissionGate>
      </PageHeaderActions>

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={handleSubmit} noValidate>
          <Hero name={values.name} initial={initial} />

          <Section
            icon={<LockRoundedIcon sx={{ fontSize: 18 }} />}
            title="계정 정보"
            description="로그인에 사용할 ID와 비밀번호를 설정합니다."
          >
            <FieldGrid>
              <TextField
                size="small"
                label="로그인 ID"
                required
                value={values.loginId}
                onChange={(e) => update('loginId', e.target.value)}
                onBlur={validation.onBlur('loginId')}
                error={validation.isInvalid('loginId') || loginIdStatus === 'taken'}
                helperText={
                  validation.errorMessage('loginId')
                    ?? loginIdStatusText(loginIdStatus)
                }
                placeholder="employee01"
                slotProps={{
                  input: { endAdornment: renderLoginIdStatusIcon(loginIdStatus) },
                }}
              />
              <TextField
                size="small"
                type="password"
                label="비밀번호"
                required
                value={values.password}
                onChange={(e) => update('password', e.target.value)}
                onBlur={validation.onBlur('password')}
                error={validation.isInvalid('password')}
                helperText={validation.errorMessage('password') ?? '4자 이상'}
                slotProps={{ htmlInput: { minLength: 4 } }}
              />
              <FieldCol2>
                <TextField
                  fullWidth
                  size="small"
                  type="password"
                  label="비밀번호 확인"
                  required
                  value={values.passwordConfirm}
                  onChange={(e) => update('passwordConfirm', e.target.value)}
                  onBlur={validation.onBlur('passwordConfirm')}
                  error={validation.isInvalid('passwordConfirm')}
                  helperText={validation.errorMessage('passwordConfirm')}
                />
              </FieldCol2>
            </FieldGrid>
          </Section>

          <Section
            icon={<PersonRoundedIcon sx={{ fontSize: 18 }} />}
            title="기본 정보"
            description="이름과 연락처 등 직원 식별 정보를 입력합니다."
          >
            <FieldGrid>
              <TextField
                size="small"
                label="이름"
                required
                value={values.name}
                onChange={(e) => update('name', e.target.value)}
                onBlur={validation.onBlur('name')}
                error={validation.isInvalid('name')}
                helperText={validation.errorMessage('name')}
              />
              <TextField
                size="small"
                type="email"
                label="이메일"
                value={values.email}
                onChange={(e) => update('email', e.target.value)}
                onBlur={validation.onBlur('email')}
                error={validation.isInvalid('email')}
                helperText={validation.errorMessage('email')}
                placeholder="name@example.com"
              />
              <TextField
                size="small"
                type="tel"
                label="연락처"
                value={values.phone}
                onChange={(e) => update('phone', e.target.value)}
                placeholder="010-0000-0000"
              />
              <TextField
                size="small"
                type="date"
                label="입사일"
                value={values.joinDate}
                onChange={(e) => update('joinDate', e.target.value)}
                slotProps={{ inputLabel: { shrink: true } }}
              />
            </FieldGrid>
          </Section>

          <Section
            icon={<BadgeRoundedIcon sx={{ fontSize: 18 }} />}
            title="소속 정보"
            description="권한과 부서 / 직책을 지정합니다. 재직 상태는 등록 시 ACTIVE 로 시작됩니다."
          >
            <FieldGrid>
              <TextField
                select
                size="small"
                label="권한"
                required
                value={values.roleId}
                onChange={(e) => update('roleId', e.target.value)}
                onBlur={validation.onBlur('roleId')}
                error={validation.isInvalid('roleId')}
                helperText={validation.errorMessage('roleId')}
              >
                {(roles ?? []).map((r) => (
                  <MenuItem key={r.id} value={String(r.id)}>
                    {r.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                select
                size="small"
                label="부서"
                value={values.departmentId}
                onChange={(e) => update('departmentId', e.target.value)}
              >
                <MenuItem value="">-</MenuItem>
                {(departments ?? []).map((d) => (
                  <MenuItem key={d.id} value={String(d.id)}>
                    {d.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                select
                size="small"
                label="직책"
                value={values.positionId}
                onChange={(e) => update('positionId', e.target.value)}
              >
                <MenuItem value="">-</MenuItem>
                {(positions ?? []).map((p) => (
                  <MenuItem key={p.id} value={String(p.id)}>
                    {p.name}
                  </MenuItem>
                ))}
              </TextField>
            </FieldGrid>
          </Section>

          <Section
            icon={<PlaceRoundedIcon sx={{ fontSize: 18 }} />}
            title="주소 정보"
            description="우편번호와 기본 주소 및 상세주소를 입력합니다."
          >
            <FieldGrid>
              <AddressSearchRow>
                <TextField
                  size="small"
                  label="우편번호"
                  value={values.zipCode}
                  placeholder="00000"
                  slotProps={{ input: { readOnly: true } }}
                  sx={{ flex: 1 }}
                />
                <AddressSearchButton
                  type="button"
                  variant="outlined"
                  startIcon={<SearchRoundedIcon sx={{ fontSize: 18 }} />}
                  onClick={handleAddressSearch}
                >
                  주소 검색
                </AddressSearchButton>
              </AddressSearchRow>
              <FieldFull>
                <TextField
                  fullWidth
                  size="small"
                  label="기본 주소"
                  value={values.roadAddress}
                  slotProps={{ input: { readOnly: true } }}
                />
              </FieldFull>
              <FieldFull>
                <TextField
                  fullWidth
                  size="small"
                  label="상세 주소"
                  value={values.detailAddress}
                  onChange={(e) => update('detailAddress', e.target.value)}
                />
              </FieldFull>
            </FieldGrid>
          </Section>
        </CreateForm>
      </CreateRoot>
    </>
  );
}

/* --------------------------------------------------------------------------
 * Hero — 라이브 아바타 + 타이틀
 * ------------------------------------------------------------------------ */

function Hero({ name, initial }: { name: string; initial: string }) {
  return (
    <HeroCard>
      {initial ? (
        <HeroAvatar>{initial}</HeroAvatar>
      ) : (
        <HeroAvatarPlaceholder>
          <ApartmentRoundedIcon sx={{ fontSize: 28 }} />
        </HeroAvatarPlaceholder>
      )}
      <HeroBody>
        <HeroEyebrow>새 직원</HeroEyebrow>
        <HeroTitle>{name.trim() || '직원 등록'}</HeroTitle>
        <HeroSubtitle>
          필수 항목을 입력한 뒤 우측 상단의 등록 버튼을 눌러주세요.
        </HeroSubtitle>
      </HeroBody>
    </HeroCard>
  );
}

/* --------------------------------------------------------------------------
 * Section card wrapper
 * ------------------------------------------------------------------------ */

interface SectionProps {
  icon: ReactNode;
  title: string;
  description?: string;
  children: ReactNode;
}

function Section({ icon, title, description, children }: SectionProps) {
  return (
    <SectionSurface>
      <SectionHeader>
        <SectionIcon>{icon}</SectionIcon>
        <SectionTitleBox>
          <SectionTitle>{title}</SectionTitle>
          {description && <SectionDescription>{description}</SectionDescription>}
        </SectionTitleBox>
      </SectionHeader>
      {children}
    </SectionSurface>
  );
}

/* --------------------------------------------------------------------------
 * 로그인 ID 가용성 표시 헬퍼
 * ------------------------------------------------------------------------ */

function loginIdStatusText(status: LoginIdStatus): string | undefined {
  switch (status) {
    case 'checking':
      return '확인 중...';
    case 'available':
      return '사용 가능한 ID 입니다.';
    case 'taken':
      return '이미 사용 중인 ID 입니다.';
    case 'idle':
    default:
      return undefined;
  }
}

function renderLoginIdStatusIcon(status: LoginIdStatus): ReactNode {
  switch (status) {
    case 'checking':
      return <CircularProgress size={14} />;
    case 'available':
      return <CheckCircleRoundedIcon color="success" sx={{ fontSize: 18 }} />;
    case 'taken':
      return <ErrorRoundedIcon color="error" sx={{ fontSize: 18 }} />;
    case 'idle':
    default:
      return null;
  }
}
