import { api } from '@/shared/api/baseApi';
import { cleanParams } from '@/shared/api/cleanParams';
import type { PageResponse } from '@/shared/types/api';
import type {
  Attendance,
  AttendanceSearchParams,
  CheckInRequest,
  CheckOutRequest,
  EmployeeLeaveBalance,
  Leave,
  LeaveBalance,
  LeaveBalanceAdjustRequest,
  LeaveCreateRequest,
  LeaveSearchParams,
  LeaveSummary,
  MyMonthlyAttendanceParams,
} from '@/features/attendance/types';

const attendanceApi = api.injectEndpoints({
  endpoints: (builder) => ({
    checkIn: builder.mutation<Attendance, CheckInRequest>({
      query: (body) => ({ url: '/api/v1/attendances/check-in', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Attendance', id: 'MY' },
        { type: 'Attendance', id: 'LIST' },
      ],
    }),
    checkOut: builder.mutation<Attendance, CheckOutRequest>({
      query: (body) => ({ url: '/api/v1/attendances/check-out', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Attendance', id: 'MY' },
        { type: 'Attendance', id: 'LIST' },
      ],
    }),
    getMyMonthlyAttendances: builder.query<Attendance[], MyMonthlyAttendanceParams>({
      query: ({ year, month }) => ({
        url: '/api/v1/attendances/me',
        method: 'GET',
        params: { year, month },
      }),
      providesTags: [{ type: 'Attendance', id: 'MY' }],
    }),
    /**
     * 전 직원 근태 현황 (관리자) — BE 는 year / month 필수.
     * 필터의 "전체" (null) 는 현재 년 / 월로 fallback 매핑해 전송한다.
     */
    getAttendances: builder.query<PageResponse<Attendance>, AttendanceSearchParams>({
      query: (params) => {
        const now = new Date();
        return {
          url: '/api/v1/attendances',
          method: 'GET',
          params: cleanParams({
            ...params,
            year: params.year ?? now.getFullYear(),
            month: params.month ?? now.getMonth() + 1,
          }),
        };
      },
      providesTags: [{ type: 'Attendance', id: 'LIST' }],
    }),

    createLeave: builder.mutation<number, LeaveCreateRequest>({
      query: (body) => ({ url: '/api/v1/leaves', method: 'POST', data: body }),
      invalidatesTags: [
        { type: 'Leave', id: 'MY' },
        { type: 'Leave', id: 'LIST' },
        { type: 'LeaveBalance', id: 'MY' },
      ],
    }),
    getMyLeaves: builder.query<Leave[], void>({
      query: () => ({ url: '/api/v1/leaves/me', method: 'GET' }),
      providesTags: [{ type: 'Leave', id: 'MY' }],
    }),
    /** 잔여 연차 — year 미전송 시 BE 가 현재 연도 기본값 처리. */
    getMyLeaveBalance: builder.query<LeaveBalance, void>({
      query: () => ({ url: '/api/v1/leaves/balance/me', method: 'GET' }),
      providesTags: [{ type: 'LeaveBalance', id: 'MY' }],
    }),

    /** 전 직원 휴가 검색 (관리자, CAN_WRITE) — startDate / endDate 는 휴가 기간 겹침 검색. */
    getLeaves: builder.query<PageResponse<LeaveSummary>, LeaveSearchParams>({
      query: (params) => ({
        url: '/api/v1/leaves',
        method: 'GET',
        params: cleanParams(params),
      }),
      providesTags: [{ type: 'Leave', id: 'LIST' }],
    }),
    /** 전 직원 잔여 연차 (관리자, CAN_WRITE). */
    getLeaveBalances: builder.query<EmployeeLeaveBalance[], { year: number }>({
      query: ({ year }) => ({
        url: '/api/v1/leaves/balances',
        method: 'GET',
        params: { year },
      }),
      providesTags: [{ type: 'LeaveBalance', id: 'LIST' }],
    }),
    /** 부여 조정 (관리자, CAN_WRITE) — 조정 대상이 본인일 수 있어 MY 잔여도 함께 invalidate. */
    adjustLeaveBalance: builder.mutation<
      void,
      { employeeId: number; body: LeaveBalanceAdjustRequest }
    >({
      query: ({ employeeId, body }) => ({
        url: `/api/v1/leaves/balances/${employeeId}`,
        method: 'PUT',
        data: body,
      }),
      invalidatesTags: [
        { type: 'LeaveBalance', id: 'LIST' },
        { type: 'LeaveBalance', id: 'MY' },
      ],
    }),
  }),
});

export const {
  useCheckInMutation,
  useCheckOutMutation,
  useGetMyMonthlyAttendancesQuery,
  useGetAttendancesQuery,
  useCreateLeaveMutation,
  useGetMyLeavesQuery,
  useGetMyLeaveBalanceQuery,
  useGetLeavesQuery,
  useGetLeaveBalancesQuery,
  useAdjustLeaveBalanceMutation,
} = attendanceApi;
