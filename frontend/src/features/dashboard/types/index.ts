import type { CustomerStatus, CustomerType } from '@/features/customer/types';
import {
  CUSTOMER_STATUS_LABELS,
  CUSTOMER_TYPE_LABELS,
} from '@/features/customer/types';

export interface DashboardKpi {
  totalCustomers: number;
  totalSalesContacts: number;
  activeEmployees: number;
  monthlySalesActivities: number;
}

export interface RecentCustomer {
  id: number;
  code: string;
  name: string;
  type: CustomerType;
  status: CustomerStatus;
  createdAt: string;
}

export type SalesActivityType = 'VISIT' | 'CALL' | 'MEETING' | 'EMAIL' | 'OTHER';

export const SALES_ACTIVITY_TYPE_LABELS: Record<SalesActivityType, string> = {
  VISIT: '방문',
  CALL: '전화',
  MEETING: '미팅',
  EMAIL: '이메일',
  OTHER: '기타',
};

export interface RecentSalesActivity {
  id: number;
  customerId: number;
  customerCode: string | null;
  customerName: string | null;
  type: SalesActivityType;
  subject: string;
  activityDate: string;
  ourEmployeeId: number;
  ourEmployeeName: string | null;
}

export interface DashboardSummary {
  kpi: DashboardKpi;
  recentCustomers: RecentCustomer[];
  recentActivities: RecentSalesActivity[];
}

// --- 권한별 위젯 (CONTRACTS / AFTER_SERVICES / EQUIPMENTS read) ---

export interface MonthlyContractStat {
  /** "2026-01" 형식 */
  month: string;
  count: number;
  totalAmount: number;
}

export interface ContractOutstandingSummary {
  totalFinalAmount: number;
  totalPaidAmount: number;
  totalOutstandingAmount: number;
}

export interface DashboardSales {
  monthlyStats: MonthlyContractStat[];
  outstanding: ContractOutstandingSummary;
}

export interface ServiceTypeStat {
  type: string;
  typeLabel: string;
  count: number;
  expenseTotal: number;
}

export interface EngineerExpenseStat {
  engineerId: number;
  engineerName: string | null;
  expenseTotal: number;
}

export interface DashboardServiceStats {
  typeStats: ServiceTypeStat[];
  engineerStats: EngineerExpenseStat[];
}

export interface ExpiringWarrantyItem {
  equipmentId: number;
  customerName: string | null;
  productModelName: string | null;
  serialNo: string | null;
  oscillatorWarrantyEndDate: string | null;
  generalWarrantyEndDate: string | null;
}

export { CUSTOMER_STATUS_LABELS, CUSTOMER_TYPE_LABELS };
