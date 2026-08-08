import { beforeEach, describe, expect, it, vi } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { renderWithTheme } from '@/test/renderWithTheme';
import type {
  DashboardSales,
  DashboardServiceStats,
  DashboardSummary,
  ExpiringWarrantyItem,
} from '@/features/dashboard/types';
import type { EmployeeProfileResponse } from '@/features/employee/types';
import DashboardPage from './DashboardPage';

const mocks = vi.hoisted(() => ({
  useDashboardPage: vi.fn(),
  navigateCustomers: vi.fn(),
}));

vi.mock('@/features/dashboard/hooks/useDashboardPage', () => ({
  useDashboardPage: mocks.useDashboardPage,
}));

vi.mock('@/features/demo/components/DemoExperienceGuide', () => ({
  default: () => <div>추천 체험 흐름</div>,
}));

const profile: EmployeeProfileResponse = {
  id: 1,
  loginId: 'demo.manager',
  name: '김서현',
  departmentName: '경영지원팀',
  positionName: '팀장',
  roleName: '관리자',
  roleCode: 'MANAGER',
  menuPermissions: [],
};

const summary: DashboardSummary = {
  kpi: {
    totalCustomers: 12,
    totalSalesContacts: 23,
    activeEmployees: 18,
    monthlySalesActivities: 31,
  },
  recentCustomers: [
    {
      id: 10,
      code: 'C-010',
      name: '한빛정밀',
      type: 'KEY_ACCOUNT',
      status: 'ACTIVE',
      createdAt: '2026-08-11T09:00:00',
    },
  ],
  recentActivities: [
    {
      id: 20,
      customerId: 10,
      customerCode: 'C-010',
      customerName: '한빛정밀',
      type: 'MEETING',
      subject: '3분기 유지보수 일정 협의',
      activityDate: '2026-08-11T10:00:00',
      ourEmployeeId: 1,
      ourEmployeeName: '김서현',
    },
  ],
};

const sales: DashboardSales = {
  monthlyStats: [
    { month: '2026-07', count: 2, totalAmount: 20_000_000 },
    { month: '2026-08', count: 3, totalAmount: 30_000_000 },
  ],
  outstanding: {
    totalFinalAmount: 50_000_000,
    totalPaidAmount: 35_000_000,
    totalOutstandingAmount: 15_000_000,
  },
};

const service: DashboardServiceStats = {
  typeStats: [
    { type: 'REPAIR', typeLabel: '수리', count: 4, expenseTotal: 1_200_000 },
    { type: 'TRAINING', typeLabel: '교육', count: 0, expenseTotal: 0 },
  ],
  engineerStats: [
    { engineerId: 1, engineerName: '박현우', expenseTotal: 700_000 },
  ],
};

const warranty: ExpiringWarrantyItem[] = [
  {
    equipmentId: 30,
    customerName: '한빛정밀',
    productModelName: '레이저 마킹기 LM-500',
    serialNo: 'LM5-260012',
    oscillatorWarrantyEndDate: '2026-09-30',
    generalWarrantyEndDate: '2026-09-15',
  },
];

interface DashboardWidgets {
  sales?: DashboardSales;
  service?: DashboardServiceStats;
  warranty?: ExpiringWarrantyItem[];
}

function dashboardState(widgets: DashboardWidgets = { sales, service, warranty }) {
  return {
    queries: {
      profile: { data: profile, isLoading: false },
      summary: { data: summary, isLoading: false },
    },
    widgets,
    monthLabel: '8월',
    onNavigateCustomers: mocks.navigateCustomers,
    onNavigateCustomer: vi.fn(),
    onNavigateSalesContacts: vi.fn(),
    onNavigateEmployees: vi.fn(),
    onNavigateSalesCustomers: vi.fn(),
    onNavigateSalesCustomer: vi.fn(),
  };
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.useDashboardPage.mockReturnValue(dashboardState());
  });

  it('핵심 지표, 운영 흐름, 최근 변화를 명확한 순서로 렌더링한다', () => {
    renderWithTheme(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(screen.getByText('한눈에 보기')).toBeInTheDocument();
    expect(screen.getByText('운영 흐름')).toBeInTheDocument();
    expect(screen.getByText('최근 변화')).toBeInTheDocument();
    expect(screen.getByText('계약과 수금')).toBeInTheDocument();
    expect(screen.getByText('AS 운영')).toBeInTheDocument();
    expect(screen.getByText('확인할 보증 일정')).toBeInTheDocument();
    expect(screen.getByText('고객 담당자')).toBeInTheDocument();
    expect(screen.getByRole('progressbar', { name: '수금 진행률' })).toHaveAttribute(
      'aria-valuenow',
      '70',
    );
    expect(screen.getByRole('region', { name: '한눈에 보기' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '운영 흐름', level: 2 })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: '계약과 수금', level: 3 })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /최근 등록 고객사/, level: 3 })).toBeInTheDocument();
  });

  it('KPI 전체가 이동 가능한 명시적 버튼으로 동작한다', async () => {
    const user = userEvent.setup();
    renderWithTheme(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('button', { name: '관리 고객사 12개사 보기' }));
    expect(mocks.navigateCustomers).toHaveBeenCalledOnce();
  });

  it('권한별 데이터가 없으면 운영 섹션만 생략하고 공통 요약은 유지한다', () => {
    mocks.useDashboardPage.mockReturnValue(
      dashboardState({ sales: undefined, service: undefined, warranty: undefined }),
    );

    renderWithTheme(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(screen.queryByText('운영 흐름')).not.toBeInTheDocument();
    expect(screen.getByText('한눈에 보기')).toBeInTheDocument();
    expect(screen.getByText('최근 변화')).toBeInTheDocument();
  });

  it('조회 권한이 없는 KPI와 최근 업무 정보는 화면에 노출하지 않는다', () => {
    mocks.useDashboardPage.mockReturnValue({
      ...dashboardState(),
      queries: {
        profile: { data: profile, isLoading: false },
        summary: { data: { kpi: {} }, isLoading: false },
      },
      onNavigateCustomers: undefined,
      onNavigateCustomer: undefined,
      onNavigateSalesContacts: undefined,
      onNavigateEmployees: undefined,
      onNavigateSalesCustomers: undefined,
      onNavigateSalesCustomer: undefined,
    });

    renderWithTheme(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(screen.queryByText('한눈에 보기')).not.toBeInTheDocument();
    expect(screen.queryByText('최근 변화')).not.toBeInTheDocument();
    expect(screen.queryByText('12')).not.toBeInTheDocument();
    expect(screen.queryByText('23')).not.toBeInTheDocument();
    expect(screen.queryByText('C-010')).not.toBeInTheDocument();
    expect(screen.queryByText('3분기 유지보수 일정 협의')).not.toBeInTheDocument();
  });

  it('권한 있는 보조 현황 조회가 실패하면 오류와 재시도 동작을 표시한다', async () => {
    const user = userEvent.setup();
    const refetch = vi.fn();
    mocks.useDashboardPage.mockReturnValue({
      ...dashboardState({ sales: undefined, service: undefined, warranty: undefined }),
      queries: {
        profile: { data: profile, isLoading: false },
        summary: { data: summary, isLoading: false },
        sales: {
          data: undefined,
          isLoading: false,
          isError: true,
          error: { status: 503, message: '계약 현황을 불러오지 못했습니다.' },
          refetch,
        },
      },
    });

    renderWithTheme(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    );

    expect(screen.getByText('계약 현황을 불러오지 못했습니다.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(refetch).toHaveBeenCalledOnce();
  });
});
